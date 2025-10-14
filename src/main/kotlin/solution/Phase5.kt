package solution

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import kotlin.io.use

@Serializable
data class Quote(
    val id: Int,
    val quote: String,
    val author: String
)

@Serializable
data class QuotesResponse(
    @SerialName("quotes") val quotes: List<Quote>
)

fun ktorQuoteClient(): HttpClient = HttpClient(CIO) {
    install(HttpTimeout) {
        requestTimeoutMillis = 5_000
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            }
        )
    }
}

suspend fun fetchQuotesWithKtorClient(url: String, client: HttpClient = ktorQuoteClient()): QuotesResponse {
    return runCatching { client.get(url).body<QuotesResponse>() }
        .getOrElse { error ->
            val message = when (error) {
                is ClientRequestException -> {
                    val status = error.response.status.value
                    val body = error.response.bodyAsText()
                    "Ktor client received HTTP $status: $body"
                }
                is ServerResponseException -> {
                    val status = error.response.status.value
                    val body = error.response.bodyAsText()
                    "Ktor server error HTTP $status: $body"
                }
                is HttpRequestTimeoutException -> "Ktor client timed out: ${error.message}"
                else -> "Ktor client request failed: ${error.message}"
            }
            throw RuntimeException(message, error)
        }
}

fun main() = runBlocking {
    ktorQuoteClient().use { client ->
        val response = fetchQuotesWithKtorClient(QUOTES_ENDPOINT, client)

        println("${response.quotes.size} quotes")

        response.quotes.forEach { quote ->
            println("- \"${quote.quote}\" (${quote.author})")
        }
    }
}
