package solution

import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

const val QUOTES_ENDPOINT = "https://dummyjson.com/quotes?limit=5"

fun fetchQuotesWithHttpUrlConnection(endpoint: String = QUOTES_ENDPOINT): String {
    val url = URL(endpoint)
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 5_000
        readTimeout = 5_000
        setRequestProperty("Accept", "application/json")
    }
    return try {
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
        if (status in 200..299) body else throw RuntimeException("HTTP $status: $body")
    } finally {
        connection.disconnect()
    }
}
