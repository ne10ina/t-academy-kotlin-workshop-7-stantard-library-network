package solution

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

fun fetchQuotesWithHttpClient(proxyUrl: String): String {
    val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    val request = HttpRequest.newBuilder()
        .uri(URI.create(proxyUrl))
        .header("Accept", "application/json")
        .GET()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() !in 200..299) {
        throw RuntimeException("Proxy HTTP ${response.statusCode()}")
    }

    return response.body()
}

fun main() {
    val proxyUrl = "http://localhost:8080/quotes"
    val result = runCatching { fetchQuotesWithHttpClient(proxyUrl) }

    result.onSuccess { body ->
        println(body)
    }.onFailure { error ->
        System.err.println(error.message)
    }
}
