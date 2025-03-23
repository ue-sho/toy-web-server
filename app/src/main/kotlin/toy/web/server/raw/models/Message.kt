package toy.web.server.raw.models

import kotlinx.serialization.json.Json

data class Message(
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        private val json = Json {
            prettyPrint = true
            isLenient = true
        }

        fun fromJson(jsonString: String): Message {
            val map = json.parseToJsonElement(jsonString).let {
                it.toString().removeSurrounding("{", "}").split(",").associate { pair ->
                    val (key, value) = pair.split(":", limit = 2)
                    key.trim().removeSurrounding("\"") to value.trim().removeSurrounding("\"")
                }
            }
            return Message(
                text = map["text"] ?: throw IllegalArgumentException("Missing text field"),
                timestamp = map["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
            )
        }

        fun toJson(message: Message): String {
            return """{"text":"${message.text}","timestamp":${message.timestamp}}"""
        }
    }
}