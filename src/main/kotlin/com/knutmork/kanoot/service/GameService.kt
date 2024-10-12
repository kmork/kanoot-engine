package com.knutmork.kanoot.service

import com.knutmork.kanoot.model.Game
import java.util.UUID

class GameService {
    private val games = mutableListOf<Game>()

    fun createGame(): Game {
        val id = UUID.randomUUID().toString()
        val pin = generatePin()
        val game = Game(id, pin)
        games.add(game)
        return game
    }

    private fun generatePin(): String {
        return (1000..9999).random().toString()
    }
}