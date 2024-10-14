package com.knutmork.kanoot.gameserver.model

import kotlinx.serialization.Serializable

@Serializable
data class Leaderboard(
    val players: List<Player> = emptyList()
)