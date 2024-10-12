package com.knutmork.kanoot.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionRequest(
    val questionNumber: Int,
    val numberOfAlternatives: Int,
    val correctAlternatives: List<Int>)