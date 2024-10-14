package com.knutmork.kanoot.gameserver.model

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val questionNumber: Int,
    var timeInSeconds: Int,
    val alternatives: List<Alternative>
)

@Serializable
data class Alternative(
    val id: Int,
    val answer: String,
    val correct: Boolean
)
