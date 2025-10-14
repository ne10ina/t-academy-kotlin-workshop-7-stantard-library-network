package solution

import io.ktor.client.HttpClient

interface QuotesApi {
    suspend fun loadQuotes(): QuotesResponse
}

class KtorQuotesApi(
    private val url: String,
    private val client: HttpClient = ktorQuoteClient()
) : QuotesApi {
    override suspend fun loadQuotes(): QuotesResponse = fetchQuotesWithKtorClient(url, client)
}

class QuotesFacade(
    private val api: QuotesApi
) {
    suspend fun listAuthors(): List<String> = api.loadQuotes().quotes.map { it.author }
}
