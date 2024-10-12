package com.knutmork.kanoot.model

class GameServer {
    private val games = mutableMapOf<String, Game>()
    private val pinToUuid = mutableMapOf<String, String>()

    fun addGame(title: String): Game {
        val game = Game.init(title)
        games[game.id] = game
        pinToUuid[game.pin] = game.id
        return game
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
        return gameByUuid(uuid)?.let { game ->
            Leaderboard(game.players.sortedByDescending { player -> player.pointsTotal })
        }
    }

    fun removeGame(gameId: String) {
        gameByUuid(gameId)?.endGame()
        games.remove(gameId)
    }

    private fun gameByUuid(uuid: String): Game? {
        return games[uuid]
    }

    private fun gameByPin(pin: String): Game? {
        val uuid = pinToUuid[pin]
        return if (uuid != null) gameByUuid(uuid) else null
    }
}