package com.knutmork.kanoot.gameserver.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,) {
    var nickname: String = ""
    var pointsThisRound: Int = 0
    var pointsTotal: Int = 0
}