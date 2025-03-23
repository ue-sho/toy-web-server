package toy.web.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import toy.web.server.plugins.*
import toy.web.server.routes.*
import toy.web.server.raw.RawServer
import toy.web.server.raw.core.Response

/**
 * Main function that starts either the raw server or Ktor based on command line arguments
 * Usage:
 * - Raw server: ./gradlew run --args="raw"
 * - Ktor server: ./gradlew run --args="ktor"
 * - Default (Ktor): ./gradlew run
 */
fun main(args: Array<String>) {
    val serverType = args.firstOrNull()?.lowercase() ?: "ktor"
    val port = 8080

    when (serverType) {
        "raw" -> startRawServer(port)
        "ktor" -> startKtorServer(port)
        else -> {
            println("Unknown server type: $serverType")
            println("Usage: ./gradlew run --args=\"[raw|ktor]\"")
            return
        }
    }
}

/**
 * Starts the raw server implementation
 */
private fun startRawServer(port: Int) {
    println("Starting raw server on port $port")
    val server = RawServer(port)

    // Add test routes
    server.get("/") { _ ->
        Response.html("""
            <html>
                <body>
                    <h1>Welcome to Raw Server</h1>
                    <p>This is a simple web server implementation in Kotlin!</p>
                    <ul>
                        <li><a href="/hello">Say Hello</a></li>
                        <li><a href="/static/index.html">Static Page</a></li>
                    </ul>
                </body>
            </html>
        """.trimIndent())
    }

    server.get("/hello") { request ->
        Response.text("Hello, World!")
    }

    server.post("/echo") { request ->
        Response.text("You sent: ${request.body}")
    }

    server.start()
}

/**
 * Starts the Ktor server implementation
 */
private fun startKtorServer(port: Int) {
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
        static("/") {
            resources("static")
            defaultResource("static/index.html")
        }
    }
}