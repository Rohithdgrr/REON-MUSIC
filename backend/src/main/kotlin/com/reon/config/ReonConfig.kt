package com.reon.config

data class ReonConfig(
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080,
    val apiKey: String = System.getenv("REON_API_KEY") ?: "",
    val cachePrefix: String = System.getenv("CACHE_PREFIX") ?: "v1",
    val version: String = "0.1.0",
) {
    val apiKeyEnforced: Boolean get() = apiKey.isNotBlank()
}
