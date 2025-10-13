package solution

import com.sun.net.httpserver.HttpServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .callTimeout(5, TimeUnit.SECONDS)
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .build()

fun fetchQuotesWithOkHttp(endpoint: String = QUOTES_ENDPOINT): String {
    val request = Request.Builder()
        .url(endpoint)
        .header("Accept", "application/json")
        .build()
    okHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $body")
        return body
    }
}

fun startOkHttpProxy(port: Int = 8080): HttpServer {
    val server = HttpServer.create(InetSocketAddress(port), 0)
    server.createContext("/quotes") { exchange ->
        val result = runCatching { fetchQuotesWithOkHttp() }
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
