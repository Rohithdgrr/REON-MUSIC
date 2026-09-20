package com.reon.routes

import com.reon.config.ReonConfig
import com.reon.innertube.ErrorBody
import com.reon.innertube.MusicService
import com.reon.innertube.UpstreamException
import com.reon.plugins.requireApiKey
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

suspend fun io.ktor.server.application.ApplicationCall.cachedJson(
    config: ReonConfig,
    load: suspend () -> String,
) {
    if (!requireApiKey(config)) return
    try {
        respondText(load(), ContentType.Application.Json)
    } catch (e: UpstreamException) {
        val status = when (e.code) {
            "NOT_FOUND", "LYRICS_NOT_FOUND", "UNPLAYABLE" -> HttpStatusCode.NotFound
            "UPSTREAM_RATE_LIMITED" -> HttpStatusCode.TooManyRequests
            "UPSTREAM_UNREACHABLE", "UPSTREAM_BAD_GATEWAY" -> HttpStatusCode.BadGateway
            "UPSTREAM_AUTH", "STREAM_CIPHERED", "NO_STREAM" -> HttpStatusCode.BadGateway
            else -> HttpStatusCode.BadGateway
        }
        respond(status, ErrorBody(error = e.code, message = e.message))
    }
}

fun Route.searchRoutes(config: ReonConfig, music: MusicService) {
    route("/api/v1") {
        get("/search") {
            val q = call.request.queryParameters["q"]?.trim().orEmpty()
            if (q.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "missing ?q="))
                return@get
            }
            val filter = call.request.queryParameters["filter"]
            call.cachedJson(config) { music.searchJson(q, filter) }
        }
        get("/search/suggestions") {
            val q = call.request.queryParameters["q"]?.trim().orEmpty()
            if (q.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "missing ?q="))
                return@get
            }
            call.cachedJson(config) { music.suggestionsJson(q) }
        }
    }
}

fun Route.catalogRoutes(config: ReonConfig, music: MusicService) {
    route("/api/v1") {
        get("/home") {
            call.cachedJson(config) { music.homeJson() }
        }
        get("/albums/{id}") {
            val id = call.pathParameters["id"]?.trim().orEmpty()
            if (id.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "missing id"))
                return@get
            }
            call.cachedJson(config) { music.albumJson(id) }
        }
        get("/artists/{id}") {
            val id = call.pathParameters["id"]?.trim().orEmpty()
            if (id.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "missing id"))
                return@get
            }
            call.cachedJson(config) { music.artistJson(id) }
        }
        get("/playlists/{id}") {
            val id = call.pathParameters["id"]?.trim().orEmpty()
            if (id.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "missing id"))
                return@get
            }
            call.cachedJson(config) { music.playlistJson(id) }
        }
        get("/radio/{trackId}") {
            val id = call.pathParameters["trackId"]?.trim().orEmpty()
            if (id.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "missing trackId"))
                return@get
            }
            call.cachedJson(config) { music.radioJson(id) }
        }
    }
}

fun Route.streamRoutes(config: ReonConfig, music: MusicService) {
    route("/api/v1") {
        get("/stream/{trackId}") {
            val id = call.pathParameters["trackId"]?.trim().orEmpty()
            if (id.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "missing trackId"))
                return@get
            }
            val quality = call.request.queryParameters["quality"]?.lowercase() ?: "high"
            if (quality !in setOf("high", "medium", "low")) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "quality must be high|medium|low"))
                return@get
            }
            call.cachedJson(config) { music.streamJson(id, quality) }
        }
        get("/player/{trackId}") {
            val id = call.pathParameters["trackId"]?.trim().orEmpty()
            if (id.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "missing trackId"))
                return@get
            }
            call.cachedJson(config) { music.playerJson(id) }
        }
        get("/lyrics/{trackId}") {
            val id = call.pathParameters["trackId"]?.trim().orEmpty()
            if (id.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("BAD_REQUEST", "missing trackId"))
                return@get
            }
            call.cachedJson(config) { music.lyricsJson(id) }
        }
    }
}
