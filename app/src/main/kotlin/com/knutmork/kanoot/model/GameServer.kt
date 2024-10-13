package com.knutmork.kanoot.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameServer {
    private val games = mutableMapOf<String, Game>()
    private val pinToUuid = mutableMapOf<String, String>()

    init {
        startCleanupJob()
    }

    fun addGame(title: String): Game {
        val game = Game.init(title)
        games[game.id] = game
        pinToUuid[game.pin] = game.id
        return game
    }

    fun resetGame(gameId: String) {
        gameByUuid(gameId)?.reset()
    }

    fun addPlayerToGame(pin: String, playerName: String): Player? {
        return gameByPin(pin)?.addPlayer(playerName)
    }

    fun addQuestion(gameId: String, question: Question) {
        gameByUuid(gameId)?.addQuestion(question)
    }

    fun answerQuestion(pin: String, playerId: String, questionNumber: Int, answer: List<Int>) {
        gameByPin(pin)?.answerQuestion(playerId, questionNumber, answer)
    }

    fun showLeaderboard(uuid: String): Leaderboard? {
        return gameByUuid(uuid)?.leaderboard
    }

    fun removeGame(gameId: String) {
        gameByUuid(gameId)?.endGame()
        games.remove(gameId)
    }

    override fun toString(): String {
        return games.values.joinToString(separator = "\n") { it.toString() }
    }

    private fun gameByUuid(uuid: String): Game? {
        return games[uuid]
    }

    private fun gameByPin(pin: String): Game? {
        val uuid = pinToUuid[pin]
        return if (uuid != null) gameByUuid(uuid) else null
    }

    private fun startCleanupJob() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(3600000L) // 1 hour
                val currentTime = System.currentTimeMillis()
                games.entries.removeIf { currentTime - it.value.createdAt > 3600000L }
            }
        }
    }
}