package com.reon

import com.reon.config.ReonConfig
import com.reon.innertube.MusicService
import com.reon.plugins.SimpleRateLimit
import com.reon.routes.catalogRoutes
import com.reon.routes.searchRoutes
import com.reon.routes.streamRoutes
import com.reon.routes.systemRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun main() {
    val config = ReonConfig()
    embeddedServer(Netty, port = config.port, module = { module(config) }).start(wait = true)
}

fun Application.module(config: ReonConfig = ReonConfig()) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true })
    }
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-Reon-Key")
        anyHost()
    }
    install(CallLogging) { level = Level.INFO }
    install(SimpleRateLimit) {
        requestsPerSecond = 10
        burst = 20
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled", cause)
            call.respond(
                io.ktor.http.HttpStatusCode.InternalServerError,
                mapOf("error" to "INTERNAL"),
            )
        }
    }
    routing {
        val music = MusicService()
        systemRoutes(config)
        searchRoutes(config, music)
        catalogRoutes(config, music)
        streamRoutes(config, music)
    }
}
