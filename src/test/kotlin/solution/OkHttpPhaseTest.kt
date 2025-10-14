package solution

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class OkHttpPhaseTest {

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
    fun `fetchQuotesWithOkHttp reads response body`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"quotes":[{"id":10}]}""")
        )

        val body = fetchQuotesWithOkHttp(server.url("/quotes").toString())

        assertTrue(body.contains("\"id\":10"))
    }
}
