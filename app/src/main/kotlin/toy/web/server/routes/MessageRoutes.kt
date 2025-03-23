package toy.web.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import toy.web.server.models.Message

fun Route.messageRouting() {
    route("/api/messages") {
        get {
            val message = Message("Hello from the server!")
            call.respond(message)
        }

        post {
            val message = call.receive<Message>()
            // ここで受け取ったメッセージを処理できます（例：データベースに保存など）
            call.respond(HttpStatusCode.Created, message)
        }
    }
}