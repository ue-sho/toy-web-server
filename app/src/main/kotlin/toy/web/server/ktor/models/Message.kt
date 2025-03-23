package toy.web.server.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)