package com.reon.plugins

import com.reon.config.ReonConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.response.respond
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Minimal per-IP bucket (~10 rps, burst 20) for Phase 0.
 */
class SimpleRateLimitConfig {
    var requestsPerSecond: Int = 10
    var burst: Int = 20
}

private data class Window(val second: AtomicLong, val count: AtomicInteger)

val SimpleRateLimit =
    createApplicationPlugin(name = "SimpleRateLimit", createConfiguration = ::SimpleRateLimitConfig) {
        val windows = ConcurrentHashMap<String, Window>()
        onCall { call ->
            val cfg = pluginConfig
            val ip = call.request.local.remoteHost
            val now = Instant.now().epochSecond
            val w = windows.computeIfAbsent(ip) { Window(AtomicLong(now), AtomicInteger(0)) }
            if (w.second.get() != now) {
                w.second.set(now)
                w.count.set(1)
            } else if (w.count.incrementAndGet() > cfg.burst) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "RATE_LIMITED"))
            }
        }
    }

suspend fun ApplicationCall.requireApiKey(config: ReonConfig): Boolean {
    if (!config.apiKeyEnforced) return true
    val got = request.headers["X-Reon-Key"] ?: ""
    if (got != config.apiKey) {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "UNAUTHORIZED"))
        return false
    }
    return true
}
