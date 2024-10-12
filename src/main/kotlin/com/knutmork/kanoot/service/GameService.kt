package com.knutmork.kanoot.service

import com.knutmork.kanoot.model.Game
import com.knutmork.kanoot.model.Player
import com.knutmork.kanoot.model.Question
import kotlinx.coroutines.*
import java.util.UUID

class GameService {
    private val games = mutableMapOf<String, Game>()
    private val pinToUuid = mutableMapOf<String, String>()
    private val questionTimers = mutableMapOf<String, Job>()

    fun initGame(title: String): Game {
        val id = UUID.randomUUID().toString()
        val pin = generatePin()
        val game = Game(id, pin, title)
        games[id] = game
        pinToUuid[pin] = id
        return game
    }

    fun addPlayerToGame(pin: String, playerName: String): Player? {
        val uuid = pinToUuid[pin]
        val game = uuid?.let { gameByUuid(it) }
        return if (game != null) {
            val player = Player(UUID.randomUUID().toString(), playerName)
            game.players.add(player)
            player
        } else {
            null
        }
    }

    fun addQuestion(gameId: String, question: Question) {
        gameByUuid(gameId)?.questions?.add(question)
        startQuestionTimer(gameId, question.questionNumber)
    }

    fun answerQuestion(pin: String, playerId: String, questionNumber: Int, answer: List<Int>) {
        val game = gameByPin(pin)
        val player = game?.players?.find { it.id == playerId }
        val question = game?.questions?.find { it.questionNumber == questionNumber }
        if (player != null && question != null && question.timeInSeconds > 0) {
            val correctAnswers = question.alternatives.filter { it.correct }.map { it.id }
            val correct = correctAnswers.size == answer.size && correctAnswers.containsAll(answer)
            if (correct) {
                val points = calculatePoints(question.timeInSeconds, question.timeInSeconds)
                player.pointsThisRound += points
                player.pointsTotal += player.pointsThisRound
            }
        }
    }

    fun removeGame(gameId: String) {
        games.remove(gameId)
        questionTimers[gameId]?.cancel()
        questionTimers.remove(gameId)
    }

    private fun startQuestionTimer(gameId: String, questionNumber: Int) {
        val game = gameByUuid(gameId) ?: return
        val question = game.questions.find { it.questionNumber == questionNumber } ?: return

        val job = CoroutineScope(Dispatchers.Default).launch {
            var timeLeft = question.timeInSeconds
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            // Time is up, no more answers can be accepted
            question.timeInSeconds = 0
        }
        questionTimers[gameId] = job
    }

    private fun calculatePoints(timeLeft: Int, totalTime: Int): Int {
        return (timeLeft.toDouble() / totalTime * 100).toInt()
    }

    private fun gameByUuid(uuid: String): Game? {
        return games[uuid]
    }

    private fun gameByPin(pin: String): Game? {
        val uuid = pinToUuid[pin]
        return if (uuid != null) gameByUuid(uuid) else null
    }

    private fun generatePin(): String {
        return (1000..9999).random().toString()
    }
}