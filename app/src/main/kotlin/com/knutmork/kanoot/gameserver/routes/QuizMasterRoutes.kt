package com.knutmork.kanoot.gameserver.routes

import com.knutmork.kanoot.gameserver.model.Question
import com.knutmork.kanoot.gameserver.model.GameServer
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.quizMasterRoutes(gameService: GameServer) {
    route("/games") {
        post("/initGame") {
            val requestBody = call.receive<Map<String, String>>()
            val title = requestBody["title"] ?: return@post call.respondText("Missing title", status = HttpStatusCode.BadRequest)
            val game = gameService.addGame(title)
            call.respond(mapOf("id" to game.id, "pin" to game.pin))
        }

        post("/{uuid}/startGame") {
            call.respondText("Game Started")
        }

        post("/{uuid}/endGame") {
            call.respondText("Game Ended")
        }

        get("/{uuid}/leaderboard") {
            val uuid = call.parameters["uuid"] ?: return@get call.respondText("Missing Game UUID", status = HttpStatusCode.BadRequest)
            val leaderboard = gameService.showLeaderboard(uuid)
            if (leaderboard != null) {
                val leaderboardResponse = leaderboard.players.map { player ->
                    mapOf("playerName" to player.nickname, "pointsTotal" to player.pointsTotal)
                }
                call.respond(leaderboardResponse)
            } else {
                call.respondText("Game not found", status = HttpStatusCode.NotFound)
            }
        }

        post("/{uuid}/removeGame") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID", status = HttpStatusCode.BadRequest)
            gameService.removeGame(uuid)
            call.respondText("Game Removed")
        }

        post("/{uuid}/question") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID", status = HttpStatusCode.BadRequest)
            val questionRequest = call.receive<Question>()
            gameService.addQuestion(uuid, questionRequest)
            call.respond(HttpStatusCode.OK, "Question received for game $uuid")
        }
    }
}