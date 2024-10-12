package com.knutmork.kanoot.plugins

import com.knutmork.kanoot.service.GameService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val gameService = GameService()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        post("/initGame") {
            val game = gameService.createGame()
            call.respond(mapOf("id" to game.id, "pin" to game.pin))
        }
        post("/startGame") {
            call.respondText("Game Started")
        }
    }
}
