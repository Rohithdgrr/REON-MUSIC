/*
 * REON Music App - Multi-Account Manager
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.services

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.accountDataStore by preferencesDataStore(name = "accounts")

@Serializable
data class UserAccount(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val isYouTubeLinked: Boolean = false,
    val youTubeChannelId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis()
)

data class AccountState(
    val accounts: List<UserAccount> = emptyList(),
    val currentAccount: UserAccount? = null,
    val isLoading: Boolean = false
)

/**
 * Multi-Account Manager
 * Manages multiple user accounts with isolated preferences
 */
@Singleton
class AccountManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AccountManager"
        private val KEY_ACCOUNTS = stringPreferencesKey("accounts")
        private val KEY_CURRENT_ACCOUNT = stringPreferencesKey("current_account_id")
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()
    
    /**
     * Load accounts from storage
     */
    suspend fun loadAccounts() {
        _state.update { it.copy(isLoading = true) }

        try {
            val prefs = context.accountDataStore.data.first()

            // Load accounts
            val accountsJson = prefs[KEY_ACCOUNTS]
            val accounts = if (accountsJson != null) {
                json.decodeFromString<List<UserAccount>>(accountsJson)
            } else {
                emptyList()
            }

            // Load current account, defaulting to the first account when the
            // stored id is missing. State is set directly (instead of routing
            // through switchAccount) so loading never re-enters account
            // switching and never performs redundant storage writes.
            val currentAccountId = prefs[KEY_CURRENT_ACCOUNT]
            var resolvedAccounts = accounts
            var currentAccount = accounts.find { it.id == currentAccountId }
            if (currentAccount == null && accounts.isNotEmpty()) {
                val first = accounts.first().copy(lastUsedAt = System.currentTimeMillis())
                currentAccount = first
                resolvedAccounts = accounts.map {
                    if (it.id == first.id) first else it
                }
                context.accountDataStore.edit { editPrefs ->
                    editPrefs[KEY_CURRENT_ACCOUNT] = first.id
                }
            }

            val finalAccounts = resolvedAccounts
            val resolvedCurrent = currentAccount
            _state.update {
                AccountState(
                    accounts = finalAccounts,
                    currentAccount = resolvedCurrent,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading accounts", e)
            _state.update { it.copy(isLoading = false) }
        }
    }
    
    /**
     * Create new account
     */
    suspend fun createAccount(name: String, email: String? = null): UserAccount {
        val newAccount = UserAccount(
            name = name,
            email = email
        )
        
        val updatedAccounts = _state.value.accounts + newAccount
        saveAccounts(updatedAccounts)

        _state.update { it.copy(accounts = updatedAccounts) }
        
        // If first account, set as current
        if (_state.value.currentAccount == null) {
            switchAccount(newAccount.id)
        }
        
        return newAccount
    }
    
    /**
     * Switch to different account
     */
    suspend fun switchAccount(accountId: String) {
        val account = _state.value.accounts.find { it.id == accountId } ?: return
        
        // Update last used time
        val updatedAccount = account.copy(lastUsedAt = System.currentTimeMillis())
        val updatedAccounts = _state.value.accounts.map {
            if (it.id == accountId) updatedAccount else it
        }
        
        saveAccounts(updatedAccounts)
        
        context.accountDataStore.edit { prefs ->
            prefs[KEY_CURRENT_ACCOUNT] = accountId
        }
        
        _state.update {
            it.copy(
                accounts = updatedAccounts,
                currentAccount = updatedAccount
            )
        }

        Log.d(TAG, "Switched to account: ${account.name}")
    }
    
    /**
     * Update account details
     */
    suspend fun updateAccount(account: UserAccount) {
        val updatedAccounts = _state.value.accounts.map {
            if (it.id == account.id) account else it
        }
        saveAccounts(updatedAccounts)
        
        val currentAccount = if (_state.value.currentAccount?.id == account.id) {
            account
        } else {
            _state.value.currentAccount
        }
        
        _state.update {
            it.copy(
                accounts = updatedAccounts,
                currentAccount = currentAccount
            )
        }
    }

    /**
     * Delete account
     */
    suspend fun deleteAccount(accountId: String) {
        val updatedAccounts = _state.value.accounts.filter { it.id != accountId }
        saveAccounts(updatedAccounts)
        
        val currentAccount = if (_state.value.currentAccount?.id == accountId) {
            updatedAccounts.firstOrNull()
        } else {
            _state.value.currentAccount
        }
        
        // Update current account if deleted
        currentAccount?.let {
            context.accountDataStore.edit { prefs ->
                prefs[KEY_CURRENT_ACCOUNT] = it.id
            }
        }
        
        _state.update {
            it.copy(
                accounts = updatedAccounts,
                currentAccount = currentAccount
            )
        }
    }

    /**
     * Link YouTube account
     */
    suspend fun linkYouTubeAccount(channelId: String) {
        val current = _state.value.currentAccount ?: return
        val updated = current.copy(
            isYouTubeLinked = true,
            youTubeChannelId = channelId
        )
        updateAccount(updated)
    }
    
    /**
     * Unlink YouTube account
     */
    suspend fun unlinkYouTubeAccount() {
        val current = _state.value.currentAccount ?: return
        val updated = current.copy(
            isYouTubeLinked = false,
            youTubeChannelId = null
        )
        updateAccount(updated)
    }
    
    /**
     * Get current account ID
     */
    fun getCurrentAccountId(): String? = _state.value.currentAccount?.id
    
    /**
     * Check if YouTube is linked for current account
     */
    fun isYouTubeLinked(): Boolean = _state.value.currentAccount?.isYouTubeLinked == true
    
    private suspend fun saveAccounts(accounts: List<UserAccount>) {
        context.accountDataStore.edit { prefs ->
            prefs[KEY_ACCOUNTS] = json.encodeToString(accounts)
        }
    }
}
