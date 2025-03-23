package toy.web.server.raw

import toy.web.server.raw.core.Server
import toy.web.server.raw.core.Request
import toy.web.server.raw.core.Response
import toy.web.server.raw.core.HttpStatus
import toy.web.server.raw.models.Message
import java.io.File

/**
 * Main server class that combines HTTP server functionality with routing
 */
class RawServer(
    private val port: Int,
    private val staticDir: String = "app/src/main/resources/static"
) {
    private val server = Server(port)

    init {
        // Set up static file serving
        server.get("/static/*") { request ->
            serveStaticFile(request.path.removePrefix("/static/"))
        }

        // Set up API routes
        setupApiRoutes()
    }

    private fun setupApiRoutes() {
        // Health check endpoint
        server.get("/api/health") { _ ->
            Response.text("OK", HttpStatus.OK)
        }

        // Messages endpoints
        server.get("/api/messages") { _ ->
            val message = Message("Hello from the server!")
            Response.json(Message.toJson(message))
        }

        server.post("/api/messages") { request ->
            try {
                val message = Message.fromJson(request.body)
                Response.json(Message.toJson(message), HttpStatus.CREATED)
            } catch (e: Exception) {
                Response.text("Invalid request body: ${e.message}", HttpStatus.BAD_REQUEST)
            }
        }
    }

    /**
     * Adds a GET route
     */
    fun get(path: String, handler: (Request) -> Response) {
        server.get(path, handler)
    }

    /**
     * Adds a POST route
     */
    fun post(path: String, handler: (Request) -> Response) {
        server.post(path, handler)
    }

    /**
     * Starts the server
     */
    fun start() {
        println("Starting raw server on port $port")
        server.start()
    }

    /**
     * Stops the server
     */
    fun stop() {
        server.stop()
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
