import com.knutmork.kanoot.gameserver.model.*
import com.knutmork.kanoot.gameserver.module
import com.knutmork.kanoot.gameserver.routes.PlayerRequest
import com.knutmork.kanoot.gameserver.routes.PlayerResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

@Serializable
data class GameResponse(val id: String, val pin: String)

class PlayerRoutesTest {

    @Test
    fun testGameSetupAndPlayerActions() = testApplication {
        application {
            module()
        }
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }

        // Create a new game
        val createGameResponse = client.post("/games/initGame") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("title" to "Game Title"))
        }
        assertEquals(HttpStatusCode.OK, createGameResponse.status)
        val game = Json.decodeFromString<GameResponse>(createGameResponse.bodyAsText())

        // Add questions to the game
        val question1 = Question(1, 10, listOf(Alternative(1, "4", true), Alternative(2, "3", false)))
        val question2 = Question(2, 10, listOf(Alternative(1, "Paris", true), Alternative(2, "London", false)))

        client.post("/games/${game.id}/question") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(question1))
        }
        client.post("/games/${game.id}/question") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(question2))
        }

        // Add two players
        runBlocking {
            client.webSocket("/play/${game.pin}") {
                val player1 = receiveDeserialized<PlayerResponse>()
                assertNotNull(player1)
            }
            client.webSocket("/play/${game.pin}") {
                val player2 = receiveDeserialized<PlayerResponse>()
                assertNotNull(player2)
            }
        }

        // Player 1 answers question 1 correctly
        runBlocking {
            client.webSocket("/play/${game.pin}") {
                sendSerialized(PlayerRequest(answer = listOf(1), nickname = "Player1"))
            }
        }

        // Player 2 answers question 1 incorrectly
        runBlocking {
            client.webSocket("/play/${game.pin}") {
                sendSerialized(PlayerRequest(answer = listOf(2), nickname = "Player2"))
            }
        }

        // Player 1 answers question 2 correctly
        runBlocking {
            client.webSocket("/play/${game.pin}") {
                sendSerialized(PlayerRequest(answer = listOf(1), nickname = "Player1"))
            }
        }

        // Player 2 answers question 2 correctly
        runBlocking {
            client.webSocket("/play/${game.pin}") {
                sendSerialized(PlayerRequest(answer = listOf(1), nickname = "Player2"))
            }
        }

        // End the game
        client.post("/games/${game.pin}/end") {
            contentType(ContentType.Application.Json)
        }

        // Verify the results
        val gameStateResponse = client.get("/games/${game.pin}/state")
        assertEquals(HttpStatusCode.OK, gameStateResponse.status)
        val gameState = Json.decodeFromString<GameState>(gameStateResponse.bodyAsText())
        assertEquals(GameState.ENDED, gameState)

        val leaderboardResponse = client.get("/games/${game.pin}/leaderboard")
        assertEquals(HttpStatusCode.OK, leaderboardResponse.status)
        val leaderboard = Json.decodeFromString<Leaderboard>(leaderboardResponse.bodyAsText())
        assertEquals(2, leaderboard.players.size)
        assertEquals("Player1", leaderboard.players[0].nickname)
        assertEquals("Player2", leaderboard.players[1].nickname)
    }
}