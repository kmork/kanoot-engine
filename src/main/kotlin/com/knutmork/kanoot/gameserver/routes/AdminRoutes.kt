package com.knutmork.kanoot.gameserver.routes

import com.knutmork.kanoot.gameserver.model.GameServer
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes(gameServer: GameServer) {
    route("/admin") {
        get("/games") {
            call.respondText(gameServer.toString())
        }

        post("/games/{uuid}/remove") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID")
            gameServer.removeGame(uuid)
            call.respondText("Game removed")
        }

        post("/games/{uuid}/reset") {
            val uuid = call.parameters["uuid"] ?: return@post call.respondText("Missing Game UUID")
            gameServer.resetGame(uuid)
            call.respondText("Game reset")
        }
    }
}