package com.knutmork.kanoot.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Game(
    val id: String,
    val pin: String,
    val title: String,
    val players: MutableList<Player> = mutableListOf(),
    val questions: MutableList<Question> = mutableListOf()
) {
    private val questionTimers = mutableMapOf<Int, Job>()

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
        return questions.lastOrNull()?.takeIf { it.timeInSeconds > 0 }
    }

    fun addQuestion(question: Question) {
        questions.add(question)
        startQuestionTimer(question.questionNumber)
    }

    fun answerQuestion(playerId: String, questionNumber: Int, answer: List<Int>) {
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

    fun addPlayer(playerName: String): Player {
        val player = Player(UUID.randomUUID().toString(), playerName)
        players.add(player)
        return player
    }

    private fun startQuestionTimer(questionNumber: Int) {
        val question = questions.find { it.questionNumber == questionNumber } ?: return

        val job = CoroutineScope(Dispatchers.Default).launch {
            var timeLeft = question.timeInSeconds
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            // Time is up, no more answers can be accepted
            question.timeInSeconds = 0
        }
        questionTimers[questionNumber] = job
    }

    private fun calculatePoints(timeLeft: Int, totalTime: Int): Int {
        return (timeLeft.toDouble() / totalTime * 100).toInt()
    }

    fun cancelTimers() {
        questionTimers.values.forEach { it.cancel() }
        questionTimers.clear()
    }
}