package toy.web.server.ktor.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import toy.web.server.raw.models.Message

fun Route.messageRouting() {
    route("/api/messages") {
        get {
            val message = Message("Hello from Ktor server!")
            call.respond(message)
        }

        post {
            try {
                val message = call.receive<Message>()
                call.respond(HttpStatusCode.Created, message)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }
    }
}