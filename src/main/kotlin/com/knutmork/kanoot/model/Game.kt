package com.knutmork.kanoot.model

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: String,
    val pin: String,
    val title: String,
    val players: MutableList<Player> = mutableListOf(),
    val questions: MutableList<Question> = mutableListOf()
) {

    fun currentQuestion(): Question? {
        return questions.lastOrNull() // TODO must check that the last one is still active
    }
}