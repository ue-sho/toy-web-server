package toy.web.server.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class MessageTest {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    @Test
    fun `test message serialization`() {
        val message = Message("Test message")
        val jsonString = json.encodeToString(message)
        assertTrue(jsonString.contains("\"text\""))
        assertTrue(jsonString.contains("\"Test message\""))
        assertTrue(jsonString.contains("\"timestamp\""))
    }

    @Test
    fun `test message deserialization`() {
        val jsonString = """{"text":"Test message","timestamp":1234567890}"""
        val message = json.decodeFromString<Message>(jsonString)
        assertEquals("Test message", message.text)
        assertEquals(1234567890, message.timestamp)
    }

    @Test
    fun `test message creation with default timestamp`() {
        val message = Message("Test message")
        assertNotNull(message.timestamp)
        assertTrue(message.timestamp > 0)
    }
}