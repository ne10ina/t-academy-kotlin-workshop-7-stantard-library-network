package solution

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

fun startHttpServerProxy(): HttpServer {
    val server = HttpServer.create(InetSocketAddress(8080), 0)

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

fun main() {
    val server = startHttpServerProxy()

    readlnOrNull()
    server.stop(0)
}
