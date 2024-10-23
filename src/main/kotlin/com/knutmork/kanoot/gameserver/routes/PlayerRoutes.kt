package com.knutmork.kanoot.gameserver.routes

import com.knutmork.kanoot.gameserver.model.GameServer
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.playerRoutes(gameServer: GameServer) {
    route("/play") {
        post("/{pin}/joinGame") {
            val pin = call.parameters["pin"] ?: return@post call.respondText("Missing Game pin", status = HttpStatusCode.BadRequest)
            val player = gameServer.addPlayer(pin)
            if (player != null) {
                call.respond(mapOf("playerId" to player.id))
            } else {
                call.respondText("Game not found", status = HttpStatusCode.NotFound)
            }
        }

        post("/{pin}/player/{playerId}/nickname") {
            val pin = call.parameters["pin"] ?: return@post call.respondText("Missing Game pin", status = HttpStatusCode.BadRequest)
            val playerId = call.parameters["playerId"] ?: return@post call.respondText("Missing player id", status = HttpStatusCode.BadRequest)
            val requestBody = call.receive<Map<String, String>>()
            val nickname = requestBody["nickname"] ?: return@post call.respondText("Missing player name", status = HttpStatusCode.BadRequest)
            val player = gameServer.setPlayerNickname(pin, playerId, nickname)
            if (player != null) {
                call.respond(mapOf("playerId" to player.id, "playerName" to player.nickname))
            } else {
                call.respondText("Player not found", status = HttpStatusCode.NotFound)
            }
        }

        post("/{pin}/answer") {
            val pin = call.parameters["pin"] ?: return@post call.respondText("Missing Game pin", status = HttpStatusCode.BadRequest)
            val parameters = call.receiveParameters()
            val playerId = parameters["playerId"] ?: return@post call.respondText("Missing player id", status = HttpStatusCode.BadRequest)
            val questionNumber = parameters["questionNumber"]?.toIntOrNull() ?: return@post call.respondText("Missing question number", status = HttpStatusCode.BadRequest)

            val answer = parameters.getAll("answer")?.mapNotNull { it.toIntOrNull() } ?: return@post call.respondText("Missing answer", status = HttpStatusCode.BadRequest)
            gameServer.answerQuestion(pin, playerId, questionNumber, answer)
            call.respondText("Answer received")
        }
    }
}