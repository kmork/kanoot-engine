package com.knutmork.kanoot.gameserver.model

import com.knutmork.kanoot.gameserver.routes.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.*

enum class GameState {
    READY,
    STARTING_QUESTION_1,
    STARTING_QUESTION_2,
    STARTING_QUESTION_3,
    STARTING_QUESTION_4,
    STARTING_QUESTION_5,
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
    private val players: MutableMap<String, Player> = mutableMapOf()
    private val questions: MutableList<Question> = mutableListOf()
    private val prepareTimers = mutableMapOf<Int, Job>()
    private val questionTimers = mutableMapOf<Int, Job>()

    val createdAt: Long = System.currentTimeMillis()

    val leaderboard: Leaderboard
        get() = Leaderboard(players.values.sortedByDescending { it.pointsTotal })

    private val _gameStateChannel = Channel<GameState>(Channel.BUFFERED)
    val gameStateChannel: ReceiveChannel<GameState> get() = _gameStateChannel

    private var _state: GameState = GameState.READY
    var state: GameState
        get() = _state
        private set(value) {
            _state = value
            CoroutineScope(Dispatchers.Default).launch {
                _gameStateChannel.send(value)
            }
        }

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

    fun reset() {
        state = GameState.READY
        cancelTimers()
    }

    override fun toString(): String {
        val playersString = players.values.joinToString(separator = ", ") { "${it.id}:${it.nickname}" }
        return "Game(id=$id, pin=$pin, titile=$title, state=$state, players=[$playersString])"
    }

    fun addPlayer(): Player {
        if (state == GameState.ENDED) {
            throw IllegalStateException("Cannot add players in the current state: $state")
        }
        val player = Player(UUID.randomUUID().toString())
        players[player.id] = player
        logger.info("New player connected and added to game $id")
        return player
    }

    fun playerById(playerId: String): Player? {
        return players[playerId]
    }

    fun addQuestion(question: Question) {
        if (state != GameState.READY && state != GameState.QUESTION_ENDED) {
            throw IllegalStateException("Cannot add questions in the current state: $state")
        }
        questions.add(question)
        logger.info("Question added to game $id")
        state = GameState.STARTING_QUESTION_1
        prepareQuestionTimer(question.questionNumber)
    }

    fun answerQuestion(player: Player, answer: List<Int>) {
        if (state != GameState.QUESTIONING) {
            throw IllegalStateException("Cannot answer questions in the current state: $state")
        }
        val question = currentQuestion() ?: run {
            logger.info("Player $player answered too late")
            return
        }

        logger.info("Player $player answered question ${question.questionNumber} with $answer")
        val correctAnswers = question.alternatives.filter { it.correct }.map { it.id }
        if (correctAnswers.size == answer.size && correctAnswers.containsAll(answer)) {
            val points = calculatePoints(question.timeInSeconds, question.timeInSeconds)
            player.pointsThisRound += points
            player.pointsTotal += player.pointsThisRound
        }
    }

    fun endGame() {
        state = GameState.ENDED
        cancelTimers()
    }

    private fun currentQuestion(): Question? {
        return questions.lastOrNull()?.takeIf { it.timeInSeconds > 0 }
    }

    private fun prepareQuestionTimer(questionNumber: Int) {
        val job = CoroutineScope(Dispatchers.Default).launch {
            for (i in 1..5) {
                state = GameState.valueOf("STARTING_QUESTION_$i")
                delay(1000L)
            }
            state = GameState.QUESTIONING
            startQuestionTimer(questionNumber)
        }
        prepareTimers[questionNumber] = job
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
            question.timeInSeconds = 0
            state = GameState.QUESTION_ENDED
        }
        questionTimers[questionNumber] = job
    }

    private fun calculatePoints(timeLeft: Int, totalTime: Int): Int {
        return (timeLeft.toDouble() / totalTime * 100).toInt()
    }

    private fun cancelTimers() {
        prepareTimers.values.forEach { it.cancel() }
        prepareTimers.clear()
        questionTimers.values.forEach { it.cancel() }
        questionTimers.clear()
    }
}