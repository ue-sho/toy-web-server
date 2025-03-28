package toy.web.server.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import toy.web.server.ktor.plugins.*
import toy.web.server.ktor.routes.*

/**
 * Starts the Ktor server implementation
 */
fun startKtorServer(port: Int) {
    println("Starting Ktor server on port $port")
    embeddedServer(Netty, port = port) {
        configureRouting()
        configureStatusPages()
        configureSerialization()
        configureMonitoring()
    }.start(wait = true)
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                text = "500: Internal Server Error\n${cause.message}",
                status = HttpStatusCode.InternalServerError,
                contentType = ContentType.Text.Plain
            )
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondText(
                text = "404: Page Not Found",
                status = HttpStatusCode.NotFound,
                contentType = ContentType.Text.Plain
            )
        }
    }
}

fun Application.configureRouting() {
    routing {
        // API routes
        messageRouting()

        get("/api/health") {
            call.respondText("OK", status = HttpStatusCode.OK)
        }

        // Static files
        staticResources("/", "static") {
            default("static/index.html")
        }
    }
}