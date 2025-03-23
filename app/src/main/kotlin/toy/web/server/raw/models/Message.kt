package toy.web.server.raw.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeFormatter

@Serializable
data class Message(
    val text: String,
    val timestamp: String = Instant.now().toString()
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(jsonString: String): Message {
            return json.decodeFromString<Message>(jsonString)
        }

        fun toJson(message: Message): String {
            return json.encodeToString(Message.serializer(), message)
        }
    }
}