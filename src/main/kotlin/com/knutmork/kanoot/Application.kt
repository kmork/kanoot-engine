package com.knutmork.kanoot

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GameRequest(val playerId: String)

@Serializable
data class GameResponse(val message: String)

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true })
        }
        routing {
            get("/initGame") {
                val request = call.receive<GameRequest>()
                call.respond(HttpStatusCode.OK, GameResponse("Game initialized for player ${request.playerId}"))
            }
            get("/startGame") {
                val request = call.receive<GameRequest>()
                call.respond(HttpStatusCode.OK, GameResponse("Game started for player ${request.playerId}"))
            }
        }
    }.start(wait = true)
}