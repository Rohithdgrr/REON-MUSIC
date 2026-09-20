package com.reon.innertube

/**
 * Phase 1 seam. v1 runs anonymous-only with 2 client configs
 * (Web Remix + Android). Rotation / circuit-breaker lands in Phase 4.
 */
enum class UpstreamClient { WEB_REMIX, ANDROID }

interface UpstreamClientSelector {
    fun current(): UpstreamClient
    fun reportFailure(client: UpstreamClient)
}

/** Round-robin stub; replace with failure-aware selector in Phase 4. */
class StaticClientSelector : UpstreamClientSelector {
    private var idx = 0
    private val order = UpstreamClient.entries.toTypedArray()

    override fun current(): UpstreamClient = order[idx % order.size]

    override fun reportFailure(client: UpstreamClient) {
        idx++
    }
}

/** Phase 4 hook. v1 logs + metrics on LOGIN_REQUIRED instead of refreshing. */
interface PoTokenProvider {
    suspend fun token(client: UpstreamClient): String?
}

class NoopPoTokenProvider : PoTokenProvider {
    override suspend fun token(client: UpstreamClient): String? = null
}

/** Phase 4 hook for ciphered streams. Null = return 502 STREAM_CIPHERED. */
interface CipherResolver {
    suspend fun decipher(videoId: String, signatureCipher: String): String?
}
