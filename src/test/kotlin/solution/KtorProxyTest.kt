package solution

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class KtorProxyTest {

    @Test
    fun `stage 4 style route works under test engine`() = testApplication {
        application {
            routing {
                get("/quotes") {
                    call.respondText(
                        text = """{"quotes":[{"id":99}]}""",
                        status = HttpStatusCode.OK,
                        contentType = ContentType.Application.Json
                    )
                }
            }
        }

        val response = client.get("/quotes")

        assertEquals(200, response.status.value)
        assertTrue(response.bodyAsText().contains("\"id\":99"))
    }
}
