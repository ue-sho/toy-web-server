package toy.web.server

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import toy.web.server.ktor.configureRouting
import toy.web.server.ktor.configureStatusPages
import toy.web.server.ktor.plugins.configureMonitoring
import toy.web.server.ktor.plugins.configureSerialization
import kotlin.test.*

class ApplicationTest {
    private fun Application.configureTestApplication() {
        configureRouting()
        configureStatusPages()
        configureSerialization()
        configureMonitoring()
    }

    @Test
    fun testHealthCheck() = testApplication {
        application {
            configureTestApplication()
        }
        client.get("/api/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("OK", bodyAsText())
        }
    }

    @Test
    fun testNotFound() = testApplication {
        application {
            configureTestApplication()
        }
        client.get("/non-existent-path").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            assertEquals("404: Page Not Found", bodyAsText())
        }
    }

    @Test
    fun testGetMessage() = testApplication {
        application {
            configureTestApplication()
        }
        client.get("/api/messages").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().contains("Hello from Ktor server!"))
        }
    }

    @Test
    fun testPostMessage() = testApplication {
        application {
            configureTestApplication()
        }
        val message = """{"text": "Test message", "timestamp": "2024-03-23T12:00:00Z"}"""
        client.post("/api/messages") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            assertTrue(bodyAsText().contains("Test message"))
        }
    }
}