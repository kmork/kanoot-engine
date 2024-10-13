package com.knutmork.kanoot.routes

import com.knutmork.kanoot.model.GameServer
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val gameService = GameServer()

    routing {
        get("/") {
            call.respondText("Hello Quiz masters!")
        }

        playerRoutes(gameService)
        quizMasterRoutes(gameService)
        adminRoutes(gameService)
    }
}
