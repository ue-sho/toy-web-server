package toy.web.server.raw

import toy.web.server.raw.core.Server
import toy.web.server.raw.core.Request
import toy.web.server.raw.core.Response
import toy.web.server.raw.core.HttpStatus
import toy.web.server.raw.models.Message
import java.io.File
import java.nio.file.Paths

/**
 * Main server class that combines HTTP server functionality with routing
 */
class RawServer(port: Int, private val staticDir: String = "static") {
    private val server = Server(port)

    init {
        // Set up static file serving
        server.get("/static/*") { request ->
            serveStaticFile(request.path.removePrefix("/static/"))
        }

        setupApiRoutes()
        setupDefaultRoutes()
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

    private fun setupDefaultRoutes() {
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
    }

    /**
     * Adds a GET route
     */
    fun get(path: String, handler: (request: Request) -> Response) {
        server.get(path, handler)
    }

    /**
     * Adds a POST route
     */
    fun post(path: String, handler: (request: Request) -> Response) {
        server.post(path, handler)
    }

    /**
     * Starts the server
     */
    fun start() {
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
        val filePath = Paths.get(staticDir, path).normalize()
        val file = File(filePath.toString())

        if (!file.exists() || !file.isFile || !file.canonicalPath.startsWith(File(staticDir).canonicalPath)) {
            return Response.text("404: File not found", HttpStatus.NOT_FOUND)
        }

        val contentType = when (file.extension.lowercase()) {
            "html" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            else -> "application/octet-stream"
        }

        return Response.file(file, contentType)
    }
}
