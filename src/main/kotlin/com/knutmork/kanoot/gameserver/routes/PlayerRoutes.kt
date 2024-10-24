package com.knutmork.kanoot.gameserver.routes

import com.knutmork.kanoot.gameserver.model.Game
import com.knutmork.kanoot.gameserver.model.GameServer
import com.knutmork.kanoot.gameserver.model.GameState
import com.knutmork.kanoot.gameserver.model.Player
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Serializable
data class PlayerRequest(val answer: List<Int>?, val nickname: String?)

@Serializable
data class PlayerResponse(val gameState: String)

val json = Json { prettyPrint = true }
val sessions = mutableMapOf<String, MutableList<DefaultWebSocketServerSession>>()

fun Application.playerRoutes(gameServer: GameServer) {
    routing {
        install(WebSockets) {
            pingPeriod = 1.minutes
            timeout = 15.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

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

            val player = game.addPlayer()
            sessions.computeIfAbsent(pin) { mutableListOf() }.add(this)

            try {
                game.gameStateChannel.consumeEach { gameState ->
                    logger.info("Game state changed to $gameState")
                    broadcastGameState(pin, gameState)
                }

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        logger.info("Received answer: $text")
                        handlePlayerRequest(player, game, json.decodeFromString<PlayerRequest>(text))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                sessions[pin]?.remove(this)
                logger.info("Closing session")
                close(CloseReason(CloseReason.Codes.NORMAL, "Session closed"))
            }
        }
    }
}

suspend fun broadcastGameState(pin: String, gameState: GameState) {
    val message = handlePlayerResponse(gameState)
    sessions[pin]?.forEach { session ->
        session.send(message)
    }
}

// TOOD: Move to Player, GameServer, or somewhere else?
fun handlePlayerRequest(player: Player, game: Game, request: PlayerRequest) {
    // Update player's nickname if provided
    request.nickname?.let {
        player.nickname = it
        logger.info("Updated player nickname to $it")
    }

    // Handle answer if provided
    if (game.state == GameState.QUESTIONING && request.answer != null) {
        game.answerQuestion(player, request.answer)
    }
}

fun handlePlayerResponse(gameState: GameState): String {
    val message = when (gameState) {
        GameState.READY -> PlayerResponse(gameState.name)
        GameState.STARTING_QUESTION_1 -> PlayerResponse(gameState.name)
        GameState.STARTING_QUESTION_2 -> PlayerResponse(gameState.name)
        GameState.STARTING_QUESTION_3 -> PlayerResponse(gameState.name)
        GameState.STARTING_QUESTION_4 -> PlayerResponse(gameState.name)
        GameState.STARTING_QUESTION_5 -> PlayerResponse(gameState.name)
        GameState.QUESTIONING -> PlayerResponse(gameState.name)
        GameState.QUESTION_ENDED -> PlayerResponse(gameState.name)
        GameState.ENDED -> PlayerResponse(gameState.name)
    }
    return json.encodeToString(message)
}