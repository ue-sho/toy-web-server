package toy.web.server.raw.core

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

/**
 * Basic TCP server implementation that handles HTTP requests
 * @param port The port number to listen on
 * @param backlog The maximum length of the queue of pending connections
 */
class Server(
    private val port: Int,
    private val backlog: Int = 50
) {
    private val serverSocket: ServerSocket = ServerSocket(port, backlog)
    private val executor = Executors.newFixedThreadPool(10)
    private var isRunning = false

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

                // Read the request line
                val requestLine = input.readLine() ?: return@execute
                println("Received request: $requestLine")

                // TODO: Implement proper HTTP request parsing and response generation
                val response = """
                    HTTP/1.1 200 OK
                    Content-Type: text/plain
                    Content-Length: 13

                    Hello, World!
                """.trimIndent()

                output.write(response.toByteArray())
                output.flush()
            } catch (e: Exception) {
                println("Error handling client request: ${e.message}")
            } finally {
                clientSocket.close()
            }
        }
    }

    /**
     * Stops the server and closes all connections
     */
    fun stop() {
        isRunning = false
        executor.shutdown()
        serverSocket.close()
    }
}