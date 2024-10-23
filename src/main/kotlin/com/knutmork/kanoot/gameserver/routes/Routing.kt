package com.knutmork.kanoot.gameserver.routes

import com.knutmork.kanoot.gameserver.model.GameServer
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val gameServer = GameServer()

    routing {
        get("/") {
            call.respondText("Hello Quiz masters!")
        }

        playerRoutes(gameServer)
        quizMasterRoutes(gameServer)
        adminRoutes(gameServer)
        configureWebSockets(gameServer)
    }
}
