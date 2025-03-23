package toy.web.server.raw.core

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

/**
 * Basic TCP server implementation that handles HTTP requests
 */
class Server(
    private val port: Int,
    private val backlog: Int = 50
) {
    private val serverSocket: ServerSocket = ServerSocket(port, backlog)
    private val executor = Executors.newFixedThreadPool(10)
    private var isRunning = false
    private val routes = mutableMapOf<Pair<HttpMethod, String>, (Request) -> Response>()

    /**
     * Adds a GET route
     */
    fun get(path: String, handler: (Request) -> Response) {
        routes[Pair(HttpMethod.GET, path)] = handler
    }

    /**
     * Adds a POST route
     */
    fun post(path: String, handler: (Request) -> Response) {
        routes[Pair(HttpMethod.POST, path)] = handler
    }

    /**
     * Starts the server and begins accepting connections
     */
    fun start() {
        isRunning = true
        println("Starting server on port $port")

        while (isRunning) {
            try {
                val clientSocket = serverSocket.accept()
                handleClient(clientSocket)
            } catch (e: Exception) {
                println("Error accepting client connection: ${e.message}")
            }
        }
    }

    /**
     * Handles a client connection in a separate thread
     */
    private fun handleClient(clientSocket: Socket) {
        executor.execute {
            try {
                val input = clientSocket.getInputStream().bufferedReader()
                val output = clientSocket.getOutputStream()

                // Read request line and headers
                val requestLines = mutableListOf<String>()
                var contentLength = 0
                var line: String?

                // Read headers
                while (input.readLine().also { line = it } != null) {
                    if (line!!.isEmpty()) break
                    requestLines.add(line!!)

                    // Check for Content-Length header
                    if (line!!.startsWith("Content-Length:", ignoreCase = true)) {
                        contentLength = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
                    }
                }

                // Build request data
                val requestData = buildString {
                    // Add request line and headers
                    append(requestLines.joinToString("\r\n"))
                    append("\r\n\r\n")

                    // Read body if Content-Length is present
                    if (contentLength > 0) {
                        val body = CharArray(contentLength)
                        input.read(body, 0, contentLength)
                        append(body)
                    }
                }

                // Parse request and handle it
                val request = Request.parse(requestData)
                val response = handleRequest(request)

                // Send response
                output.write(response.toString().toByteArray())
                output.flush()
            } catch (e: Exception) {
                println("Error handling client request: ${e.message}")
                e.printStackTrace()  // より詳細なエラー情報を出力
            } finally {
                clientSocket.close()
            }
        }
    }

    /**
     * Handles an HTTP request by finding and executing the appropriate route handler
     */
    private fun handleRequest(request: Request): Response {
        val handler = routes[Pair(request.method, request.path)]
            ?: routes.entries.find { (key, _) ->
                key.first == request.method && pathMatches(key.second, request.path)
            }?.value

        return handler?.invoke(request) ?: Response(
            status = HttpStatus.NOT_FOUND,
            body = "404 Not Found"
        )
    }

    /**
     * Checks if a request path matches a route pattern
     */
    private fun pathMatches(pattern: String, path: String): Boolean {
        return when {
            pattern == path -> true
            pattern.endsWith("/*") && path.startsWith(pattern.removeSuffix("/*")) -> true
            else -> false
        }
    }

    /**
     * Extracts Content-Length value from header line
     */
    private fun extractContentLength(headerLine: String): Int? {
        if (!headerLine.startsWith("Content-Length:", ignoreCase = true)) {
            return null
        }
        return headerLine.substringAfter(":").trim().toIntOrNull()
    }

    /**
     * Stops the server and closes all connections
     */
    fun stop() {
        isRunning = false
        try {
            serverSocket.close()
        } catch (e: Exception) {
            println("Error closing server socket: ${e.message}")
        }
        executor.shutdown()
    }
}