package com.reon.routes

import com.reon.config.ReonConfig
import com.reon.plugins.requireApiKey
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String, val version: String)

@Serializable
data class ConfigResponse(
    val version: String,
    val anonymousOnly: Boolean = true,
    val qualities: List<String> = listOf("high", "medium", "low"),
    val features: Map<String, Boolean> = mapOf(
        "search" to false,
        "home" to false,
        "catalog" to false,
        "stream" to false,
        "lyrics" to false,
        "newpipeFallback" to false,
        "sponsorblock" to false,
    ),
    val cachePrefix: String,
)

fun Route.systemRoutes(config: ReonConfig) {
    route("/api/v1") {
        get("/health") {
            call.respond(HealthResponse(status = "ok", version = config.version))
        }
        get("/config") {
            if (!call.requireApiKey(config)) return@get
            call.respond(ConfigResponse(version = config.version, cachePrefix = config.cachePrefix))
        }
    }
}
