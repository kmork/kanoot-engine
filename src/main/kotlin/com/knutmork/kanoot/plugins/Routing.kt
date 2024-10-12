package com.knutmork.kanoot.plugins

import com.knutmork.kanoot.model.QuestionRequest
import com.knutmork.kanoot.service.GameService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val gameService = GameService()

    routing {
        get("/") {
            call.respondText("Hello Quiz masters!")
        }
        post("/initGame") {
            val game = gameService.initGame()
            call.respond(mapOf("id" to game.id, "pin" to game.pin))
        }
        post("//games/{uuid}/startGame") {
            call.respondText("Game Started")
        }
        post("/games/{uuid}/endGame") {
            call.respondText("Game Ended")
        }
        post("/games/{uuid}/removeGame") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID", status = HttpStatusCode.BadRequest)
            gameService.removeGame(uuid)
            call.respondText("Game Removed")
        }
        post("/games/{uuid}/question") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID", status = HttpStatusCode.BadRequest)
            val questionRequest = call.receive<QuestionRequest>()
            // Process the questionRequest and uuid as needed
            call.respond(HttpStatusCode.OK, "Question received for game $uuid")
        }
        post("/joinGame") {
            val parameters = call.receiveParameters()
            val pin = parameters["pin"] ?: return@post call.respondText("Missing pin", status = HttpStatusCode.BadRequest)
            val playerName = parameters["name"] ?: return@post call.respondText("Missing player name", status = HttpStatusCode.BadRequest)
            val player = gameService.addPlayerToGame(pin, playerName)
            if (player != null) {
                call.respond(mapOf("playerId" to player.id, "playerName" to player.name))
            } else {
                call.respondText("Game not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
