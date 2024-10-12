package com.knutmork.kanoot.model

import kotlinx.serialization.Serializable

@Serializable
data class Leaderboard(
    val players: List<Player> = emptyList()
)