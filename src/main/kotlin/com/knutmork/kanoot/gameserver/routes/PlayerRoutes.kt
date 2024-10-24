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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Serializable
data class PlayerRequest(val answer: List<Int>, val nickname: String?)

@Serializable
data class PlayerResponse(val gameState: String)

val json = Json { prettyPrint = true }

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

            handleGameState(game.state) // Send current game state to player whenever this player has arrived in the game

            // Handle incoming request - e.g. setting nickname or answer question
            // player.nickname = nickname

            // Publish game state changes to player

            try {
                game.gameStateChannel.consumeEach { gameState ->
                    logger.info("Game state changed to $gameState")
                    handleGameState(gameState)
                }

                logger.info("Before checking incoming")

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        logger.info("Received answer: $text")
                        if (game.state == GameState.QUESTIONING) {
                            val response = json.decodeFromString<PlayerRequest>(text)
                            game.answerQuestion(player, response.answer)
                        }
                    }
                }

                logger.info("After checking incoming")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                logger.info("Closing session")
                close(CloseReason(CloseReason.Codes.NORMAL, "Session closed"))
            }
        }
    }
}

fun generateGameStateMessage(gameState: GameState): String {
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

suspend fun DefaultWebSocketServerSession.handleGameState(gameState: GameState) {
    val message = generateGameStateMessage(gameState)
    send(message)
}

