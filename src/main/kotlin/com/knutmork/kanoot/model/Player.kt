package com.knutmork.kanoot.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(val id: String, val name: String)