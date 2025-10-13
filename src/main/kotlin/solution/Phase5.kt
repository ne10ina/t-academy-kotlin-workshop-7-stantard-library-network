package solution

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
    return client.get(url).body()
}
