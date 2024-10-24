package com.knutmork.kanoot.gameserver.routes

import com.knutmork.kanoot.gameserver.model.GameServer
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Routing")

fun Application.configureRouting() {
    val gameServer = GameServer()

    routing {
        get("/") {
            call.respondText("Hello Quiz masters!")
        }

        quizMasterRoutes(gameServer)
        adminRoutes(gameServer)
        playerRoutes(gameServer)
    }
}
