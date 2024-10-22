package com.knutmork.kanoot.gameserver.routes

import com.knutmork.kanoot.gameserver.questionReceivedChannel
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GameMessage(val header: String, val subHeader: String? = null)

fun Application.configureWebSockets() {
    routing {
        webSocket("/updates") {
            val json = Json { prettyPrint = true }
            send(json.encodeToString(GameMessage(header = "You're in!", subHeader = "See your nickname on screen?")))

            // Wait for the signal that a question has been received
            questionReceivedChannel.receive()

            for (i in 5 downTo 1) {
                send(json.encodeToString(GameMessage(header = "Get ready in $i...")))
                delay(1000) // Wait for 1 second
            }
            send(json.encodeToString(GameMessage(header = "Game begins")))

            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val receivedText = frame.readText()
                    // Handle incoming messages if needed
                }
            }
        }
    }
}

