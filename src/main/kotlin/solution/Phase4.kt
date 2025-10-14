package solution

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun startKtorProxy(port: Int = 8080): ApplicationEngine {
    val engine = embeddedServer(CIO, port = port) {
        routing {
            get("/quotes") {
                val result = runCatching { fetchQuotesWithOkHttp() }

                if (result.isSuccess) {
                    call.respondText(result.getOrThrow(), ContentType.Application.Json)
                } else {
                    val message = result.exceptionOrNull()?.message ?: "upstream error"
                    call.respondText(
                        text = """{"error":"$message"}""",
                        status = HttpStatusCode.BadGateway,
                        contentType = ContentType.Application.Json
                    )
                }
            }
        }
    }

    engine.start()
    return engine
}
