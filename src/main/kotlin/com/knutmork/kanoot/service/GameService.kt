package com.knutmork.kanoot.service

import com.knutmork.kanoot.model.Game
import com.knutmork.kanoot.model.Player
import java.util.UUID

class GameService {
    private val games = mutableMapOf<String, Game>()
    private val pinToUuid = mutableMapOf<String, String>()

    fun initGame(): Game {
        val id = UUID.randomUUID().toString()
        val pin = generatePin()
        val game = Game(id, pin)
        games[id] = game
        pinToUuid[pin] = id
        return game
    }

    fun addPlayerToGame(pin: String, playerName: String): Player? {
        val uuid = pinToUuid[pin]
        val game = uuid?.let { games[it] }
        return if (game != null) {
            val player = Player(UUID.randomUUID().toString(), playerName)
            game.players.add(player)
            player
        } else {
            null
        }
    }

    fun removeGame(gameId: String) {
        games.remove(gameId)
    }

    private fun getGameByUuid(uuid: String): Game? {
        return games[uuid]
    }

    private fun getGameByPin(pin: String): Game? {
        val uuid = pinToUuid[pin]
        return if (uuid != null) games[uuid] else null
    }

    private fun generatePin(): String {
        return (1000..9999).random().toString()
    }

}