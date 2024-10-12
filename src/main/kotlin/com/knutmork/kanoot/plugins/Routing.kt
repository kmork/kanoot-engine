package com.knutmork.kanoot.plugins

import com.knutmork.kanoot.model.Question
import com.knutmork.kanoot.model.GameServer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val gameService = GameServer()

    routing {
        get("/") {
            call.respondText("Hello Quiz masters!")
        }
        post("/initGame") {
            val parameters = call.receiveParameters()
            val title = parameters["title"] ?: ""
            val game = gameService.addGame(title)
            call.respond(mapOf("id" to game.id, "pin" to game.pin))
        }

        post("/games/{uuid}/startGame") {
            call.respondText("Game Started")
        }

        post("/games/{uuid}/endGame") {
            call.respondText("Game Ended")
        }

        get("/games/{uuid}/leaderboard") {
            val uuid = call.parameters["uuid"] ?: return@get call.respondText("Missing Game UUID", status = HttpStatusCode.BadRequest)
            val leaderboard = gameService.showLeaderboard(uuid)
            if (leaderboard != null) {
                val leaderboardResponse = leaderboard.players.map { player ->
                    mapOf("playerName" to player.name, "pointsTotal" to player.pointsTotal)
                }
                call.respond(leaderboardResponse)
            } else {
                call.respondText("Game not found", status = HttpStatusCode.NotFound)
            }
        }

        post("/games/{uuid}/removeGame") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID", status = HttpStatusCode.BadRequest)
            gameService.removeGame(uuid)
            call.respondText("Game Removed")
        }

        post("/games/{uuid}/question") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID", status = HttpStatusCode.BadRequest)
            val questionRequest = call.receive<Question>()
            gameService.addQuestion(uuid, questionRequest)
            call.respond(HttpStatusCode.OK, "Question received for game $uuid")
        }

        post("/{pin}/joinGame") {
            val pin = call.parameters["pin"] ?: return@post call.respondText("Missing Game pin", status = HttpStatusCode.BadRequest)
            val parameters = call.receiveParameters()
            val playerName = parameters["name"] ?: return@post call.respondText("Missing player name", status = HttpStatusCode.BadRequest)
            val player = gameService.addPlayerToGame(pin, playerName)
            if (player != null) {
                call.respond(mapOf("playerId" to player.id, "playerName" to player.name))
            } else {
                call.respondText("Game not found", status = HttpStatusCode.NotFound)
            }
        }

        post("/{pin}/answer") {
            val pin = call.parameters["pin"] ?: return@post call.respondText("Missing Game pin", status = HttpStatusCode.BadRequest)
            val parameters = call.receiveParameters()
            val playerId = parameters["playerId"] ?: return@post call.respondText("Missing player id", status = HttpStatusCode.BadRequest)
            val questionNumber = parameters["questionNumber"]?.toIntOrNull() ?: return@post call.respondText("Missing question number", status = HttpStatusCode.BadRequest)

            val answer = parameters.getAll("answer")?.mapNotNull { it.toIntOrNull() } ?: return@post call.respondText("Missing answer", status = HttpStatusCode.BadRequest)
            gameService.answerQuestion(pin, playerId, questionNumber, answer)
            call.respondText("Answer received")
        }
    }
}
