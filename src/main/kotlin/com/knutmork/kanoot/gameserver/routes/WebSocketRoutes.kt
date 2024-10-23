package com.knutmork.kanoot.gameserver.routes

import com.knutmork.kanoot.gameserver.model.GameServer
import com.knutmork.kanoot.gameserver.model.GameState
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GameMessage(val header: String, val subHeader: String? = null)

val json = Json { prettyPrint = true }

fun Application.configureWebSockets(gameServer: GameServer) {
    routing {
        webSocket("/play/{pin}") {
            val pin = call.parameters["pin"] ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.CANNOT_ACCEPT,
                    "Missing game pin"
                )
            )
            val game = gameServer.gameByPin(pin) ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.CANNOT_ACCEPT,
                    "Invalid game pin"
                )
            )

            handleGameState(game.state)
            try {
                game.gameStateChannel.consumeEach { gameState ->
                    handleGameState(gameState)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                close(CloseReason(CloseReason.Codes.NORMAL, "Session closed"))
            }
        }
    }
}

suspend fun DefaultWebSocketServerSession.handleGameState(gameState: GameState) {
    when (gameState) {
        GameState.READY,
        GameState.WAITING_FOR_PLAYERS -> {
            send(json.encodeToString(GameMessage(header = "You're in!", subHeader = "See your nickname on screen?")))
        }
        GameState.STARTING_QUESTION_1 -> {
            send(json.encodeToString(GameMessage(header = "Get ready in 5...")))
        }
        GameState.STARTING_QUESTION_2 -> {
            send(json.encodeToString(GameMessage(header = "Get ready in 4...")))
        }
        GameState.STARTING_QUESTION_3 -> {
            send(json.encodeToString(GameMessage(header = "Get ready in 3...")))
        }
        GameState.STARTING_QUESTION_4 -> {
            send(json.encodeToString(GameMessage(header = "Get ready in 2...")))
        }
        GameState.STARTING_QUESTION_5 -> {
            send(json.encodeToString(GameMessage(header = "Get ready in 1...")))
        }
        GameState.QUESTIONING -> {
            send(json.encodeToString(GameMessage(header = "Question?")))
        }
        GameState.QUESTION_ENDED -> {
            send(json.encodeToString(GameMessage(header = "Question ended")))
        }
        GameState.ENDED -> {
            outgoing.send(Frame.Text(json.encodeToString(gameState)))
        }
    }
}

