package solution

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class QuoteFacadeTest {

    @Test
    fun `quotes facade aggregates authors via fake api`() = runBlocking {
        val fakeApi = object : QuotesApi {
            override suspend fun loadQuotes(): QuotesResponse = QuotesResponse(
                quotes = listOf(Quote(id = 1, quote = "Hello", author = "Tester"))
            )
        }

        val facade = QuotesFacade(fakeApi)

        val authors = facade.listAuthors()

        assertEquals(listOf("Tester"), authors)
    }
}
