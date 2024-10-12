package com.knutmork.kanoot.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.*

enum class GameState {
    READY,
    WAITING_FOR_PLAYERS,
    STARTING_QUESTION,
    QUESTIONING,
    QUESTION_ENDED,
    ENDED
}

@Serializable
class Game(
    val id: String,
    val pin: String,
    val title: String,
) {
    private val players: MutableList<Player> = mutableListOf()
    private val questions: MutableList<Question> = mutableListOf()
    private var state: GameState = GameState.READY
    private val questionTimers = mutableMapOf<Int, Job>()

    val createdAt: Long = System.currentTimeMillis()

    val leaderboard: Leaderboard
        get() = Leaderboard(players.sortedByDescending { it.pointsTotal })

    companion object {
        fun init(title: String): Game {
            val id = UUID.randomUUID().toString()
            val pin = generatePin()
            return Game(id, pin, title)
        }

        private fun generatePin(): String {
            return (1000..9999).random().toString()
        }
    }

    fun currentQuestion(): Question? {
        return questions.lastOrNull()?.takeIf { it.timeInSeconds > 0 } // TODO: Should we use state instead?
    }

    fun addPlayer(playerName: String): Player {
        if (state != GameState.WAITING_FOR_PLAYERS) {
            throw IllegalStateException("Cannot add players in the current state: $state")
        }
        val player = Player(UUID.randomUUID().toString(), playerName)
        players.add(player)
        return player
    }

    fun addQuestion(question: Question) {
        if (state != GameState.READY && state != GameState.QUESTION_ENDED) {
            throw IllegalStateException("Cannot add questions in the current state: $state")
        }
        questions.add(question)
        state = GameState.STARTING_QUESTION
        startQuestionTimer(question.questionNumber)
    }

    fun answerQuestion(playerId: String, questionNumber: Int, answer: List<Int>) {
        if (state != GameState.QUESTIONING) {
            throw IllegalStateException("Cannot answer questions in the current state: $state")
        }
        val player = players.find { it.id == playerId }
        val question = questions.find { it.questionNumber == questionNumber }
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

    fun endGame() {
        state = GameState.ENDED
        cancelTimers()
    }

    private fun startQuestionTimer(questionNumber: Int) {
        val question = questions.find { it.questionNumber == questionNumber } ?: return

        val job = CoroutineScope(Dispatchers.Default).launch {
            var timeLeft = question.timeInSeconds
            state = GameState.QUESTIONING
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            // Time is up, no more answers can be accepted
            question.timeInSeconds = 0
            state = GameState.QUESTION_ENDED
        }
        questionTimers[questionNumber] = job
    }

    private fun calculatePoints(timeLeft: Int, totalTime: Int): Int {
        return (timeLeft.toDouble() / totalTime * 100).toInt()
    }

    private fun cancelTimers() {
        questionTimers.values.forEach { it.cancel() }
        questionTimers.clear()
    }
}