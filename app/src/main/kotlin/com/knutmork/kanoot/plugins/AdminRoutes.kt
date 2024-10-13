package com.knutmork.kanoot.plugins

import com.knutmork.kanoot.model.GameServer
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes(gameService: GameServer) {
    route("/admin") {
        get("/games") {
            call.respondText(gameService.toString())
        }

        post("/games/{uuid}/remove") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID")
            gameService.removeGame(uuid)
            call.respondText("Game removed")
        }

        post("/games/{uuid}/reset") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID")
            gameService.resetGame(uuid)
            call.respondText("Game reset")
        }
    }
}