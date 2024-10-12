package com.knutmork.kanoot

import kotlinx.cli.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

//$ kotlin CliApp.kt add-game "My New Game"
//$ kotlin CliApp.kt add-player 1234 "Player1"
//$ kotlin CliApp.kt show-leaderboard some-uuid
fun main(args: Array<String>) {
    val parser = ArgParser("kanoot-cli")

    val command = parser.subcommands(
        AddGameCommand(),
        AddPlayerCommand(),
        ShowLeaderboardCommand()
    )

    parser.parse(args)
}

abstract class BaseCommand(name: String, description: String) : Subcommand(name, description) {
    protected val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
}

class AddGameCommand : BaseCommand("add-game", "Add a new game") {
    private val title by argument(ArgType.String, description = "Title of the game")

    override fun execute() = runBlocking {
        val response: HttpResponse = client.post("http://localhost:8080/initGame") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("title" to title))
        }
        println("Game added: ${response.bodyAsText()}")
    }
}

class AddPlayerCommand : BaseCommand("add-player", "Add a player to a game") {
    private val pin by argument(ArgType.String, description = "Game PIN")
    private val playerName by argument(ArgType.String, description = "Player name")

    override fun execute() = runBlocking {
        val response: HttpResponse = client.post("http://localhost:8080/$pin/joinGame") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("name" to playerName))
        }
        println("Player added: ${response.bodyAsText()}")
    }
}

class ShowLeaderboardCommand : BaseCommand("show-leaderboard", "Show the leaderboard of a game") {
    private val uuid by argument(ArgType.String, description = "Game UUID")

    override fun execute() = runBlocking {
        val response: HttpResponse = client.get("http://localhost:8080/games/$uuid/leaderboard")
        println("Leaderboard: ${response.bodyAsText()}")
    }
}