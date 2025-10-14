package solution

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

fun startHttpServerProxy(port: Int = 8080): HttpServer {
    val server = HttpServer.create(InetSocketAddress(port), 0)

    server.createContext("/quotes") { exchange ->
        val result = runCatching { fetchQuotesWithHttpUrlConnection() }
        val body = result.getOrElse { """{"error":"${it.message}"}""" }
        val status = if (result.isSuccess) 200 else 502

        val bytes = body.toByteArray(StandardCharsets.UTF_8)

        exchange.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }

        exchange.close()
    }

    server.executor = null
    server.start()

    return server
}

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
