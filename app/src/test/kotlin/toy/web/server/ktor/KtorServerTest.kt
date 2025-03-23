package toy.web.server.ktor

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import toy.web.server.ktor.plugins.configureMonitoring
import toy.web.server.ktor.plugins.configureSerialization
import kotlin.test.*

class ApplicationTest {

    @Test
    fun `test health endpoint returns OK`() = testApplication {
        val response = client.get("/api/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }

    @Test
    fun `test get message endpoint returns valid message`() = testApplication {
        val response = client.get("/api/messages")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Hello from the server!"))
    }

    @Test
    fun `test post message endpoint accepts valid message`() = testApplication {
        val response = client.post("/api/messages") {
            contentType(ContentType.Application.Json)
            setBody("""{"text": "Test message"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("Test message"))
    }

    @Test
    fun `test not found returns 404`() = testApplication {
        val response = client.get("/non-existent-path")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("404: Page Not Found"))
    }

    private fun testApplication(block: suspend ApplicationTestBuilder.() -> Unit) {
        io.ktor.server.testing.testApplication {
            application {
                configureRouting()
                configureStatusPages()
                configureSerialization()
                configureMonitoring()
            }
            block()
        }
    }
}