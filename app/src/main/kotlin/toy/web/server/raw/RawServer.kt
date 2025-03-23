package toy.web.server.raw

import toy.web.server.raw.core.Server
import toy.web.server.raw.core.Request
import toy.web.server.raw.core.Response
import toy.web.server.raw.routing.Router
import java.io.File

/**
 * Main server class that combines HTTP server functionality with routing
 */
class RawServer(
    private val port: Int,
    private val staticDir: String = "static"
) {
    private val server = Server(port)
    private val router = Router()

    init {
        // Set up static file serving
        router.get("/static/*") { request ->
            serveStaticFile(request.path.removePrefix("/static/"))
        }
    }

    /**
     * Adds a GET route
     */
    fun get(path: String, handler: (Request) -> Response) {
        router.get(path, handler)
    }

    /**
     * Adds a POST route
     */
    fun post(path: String, handler: (Request) -> Response) {
        router.post(path, handler)
    }

    /**
     * Starts the server
     */
    fun start() {
        println("Starting raw server on port $port")
        server.start()
    }

    /**
     * Serves a static file from the static directory
     */
    private fun serveStaticFile(path: String): Response {
        val file = File(staticDir, path)

        if (!file.exists() || !file.isFile) {
            return Response.text("404 Not Found", HttpStatus.NOT_FOUND)
        }

        val contentType = when (file.extension.lowercase()) {
            "html" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            else -> "application/octet-stream"
        }

        return Response(
            headers = mutableMapOf("Content-Type" to contentType),
            body = file.readText()
        )
    }
}

/**
 * Example usage:
 */
fun main() {
    val server = RawServer(8080)

    // Add some routes
    server.get("/") { _ ->
        Response.html("""
            <html>
                <body>
                    <h1>Welcome to Raw Server</h1>
                    <p>This is a simple web server implementation in Kotlin!</p>
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

    // Start the server
    server.start()
}