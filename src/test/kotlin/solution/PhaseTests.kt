package solution

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PhaseTests {

    private lateinit var server: MockWebServer

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @AfterTest
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    @Test
    fun `phase 1 reads body`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"quotes":[]}""")
        )

        val body = fetchQuotesWithHttpUrlConnection(server.url("/quotes").toString())

        assertEquals("""{"quotes":[]}""", body)
    }

    @Test
    fun `phase 1 handles error codes`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("fail")
        )

        val error = runCatching { fetchQuotesWithHttpUrlConnection(server.url("/quotes").toString()) }.exceptionOrNull()

        assertTrue(error is RuntimeException)
        assertTrue(error?.message?.contains("500") == true)
    }

    @Test
    fun `phase 3 okHttp fetch works`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"quotes":[{"id":1}]}""")
        )

        val body = fetchQuotesWithOkHttp(server.url("/quotes").toString())

        assertTrue(body.contains("\"id\":1"))
    }

    @Test
    fun `phase 5 deserializes response`() = runBlocking {
        val payload = QuotesResponse(
            quotes = listOf(Quote(id = 1, quote = "Hi", author = "Test"))
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(Json.encodeToString(payload))
        )

        val client = ktorQuoteClient()
        try {
            val result = fetchQuotesWithKtorClient(server.url("/quotes").toString(), client)
            assertEquals(1, result.quotes.size)
        } finally {
            client.close()
        }
    }
}
