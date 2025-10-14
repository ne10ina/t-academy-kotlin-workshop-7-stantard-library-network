package solution

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .callTimeout(5, TimeUnit.SECONDS)
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .build()

fun fetchQuotesWithOkHttp(endpoint: String = QUOTES_ENDPOINT): String {
    val request = Request.Builder()
        .get()
        .url(endpoint)
        .header("Accept", "application/json")
        .build()

    return runCatching {
        okHttpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $body")
            body
        }
    }.getOrElse { error ->
        throw RuntimeException("OkHttp request failed: ${error.message}", error)
    }
}

fun main() {
    val result = runCatching { fetchQuotesWithOkHttp() }

    result.onSuccess { body ->
        println(body)
    }.onFailure { error ->
        System.err.println(error.message)
    }
}
