package com.knutmork.kanoot.gameserver

import com.knutmork.kanoot.gameserver.routes.configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val host = System.getenv("HOST")?: "0.0.0.0"
    embeddedServer(Netty, port = port, host = host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })    }
    install(WebSockets) {
        pingPeriod = 1.minutes
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    configureRouting()
}
