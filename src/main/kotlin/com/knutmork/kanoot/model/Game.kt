package com.knutmork.kanoot.model

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: String,
    val pin: String,
    val players: MutableList<Player> = mutableListOf())