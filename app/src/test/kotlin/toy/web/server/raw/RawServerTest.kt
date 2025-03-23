package toy.web.server.raw

import toy.web.server.raw.core.Request
import toy.web.server.raw.core.Response
import toy.web.server.raw.core.HttpMethod
import toy.web.server.raw.core.HttpStatus
import toy.web.server.raw.models.Message
import kotlin.test.*
import java.net.Socket
import java.io.PrintWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread
import java.util.concurrent.atomic.AtomicBoolean

class RawServerTest {
    private lateinit var server: RawServer
    private val port = 8081
    private val isRunning = AtomicBoolean(true)
    private val serverThread = thread(start = false) {
        server.start()
        isRunning.set(false)
    }

    @BeforeTest
    fun setUp() {
        server = RawServer(port)
        setupRoutes()
        serverThread.start()
        Thread.sleep(100) // サーバーの起動を待つ
    }

    @AfterTest
    fun tearDown() {
        server.stop()  // サーバーを適切に停止
        isRunning.set(false)
        serverThread.interrupt()
        Thread.sleep(100) // サーバーの停止を待つ
    }

    private fun setupRoutes() {
        // APIルートの設定
        server.get("/api/health") { _ ->
            Response.text("OK", HttpStatus.OK)
        }

        server.get("/api/messages") { _ ->
            val message = Message("Hello from the server!")
            Response.json(Message.toJson(message))
        }

        server.post("/api/messages") { request ->
            val message = Message.fromJson(request.body)
            Response.json(Message.toJson(message), HttpStatus.CREATED)
        }
    }

    @Test
    fun `test health endpoint returns OK`() {
        val response = sendRequest("GET", "/api/health")
        assertTrue(response.contains("200 OK"))
        assertTrue(response.contains("OK"))
    }

    @Test
    fun `test get message endpoint returns valid message`() {
        val response = sendRequest("GET", "/api/messages")
        assertTrue(response.contains("200 OK"))
        assertTrue(response.contains("Hello from the server!"))
    }

    @Test
    fun `test post message endpoint accepts valid message`() {
        val response = sendRequest(
            "POST",
            "/api/messages",
            """{"text": "Test message"}""",
            mapOf("Content-Type" to "application/json")
        )
        assertTrue(response.contains("201 Created"))
        assertTrue(response.contains("Test message"))
    }

    @Test
    fun `test not found returns 404`() {
        val response = sendRequest("GET", "/non-existent-path")
        assertTrue(response.contains("404 Not Found"))
    }

    private fun sendRequest(
        method: String,
        path: String,
        body: String = "",
        headers: Map<String, String> = emptyMap()
    ): String {
        var retries = 3
        var lastException: Exception? = null

        while (retries > 0 && isRunning.get()) {
            try {
                return Socket("localhost", port).use { socket ->
                    val writer = PrintWriter(socket.getOutputStream(), true)
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    // Send request line
                    writer.println("$method $path HTTP/1.1")

                    // Send headers
                    writer.println("Host: localhost:$port")
                    headers.forEach { (key, value) ->
                        writer.println("$key: $value")
                    }
                    if (body.isNotEmpty()) {
                        writer.println("Content-Length: ${body.length}")
                    }
                    writer.println()  // Empty line to separate headers from body

                    // Send body if present
                    if (body.isNotEmpty()) {
                        writer.print(body)
                        writer.flush()
                    }

                    // Read response
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line).append("\r\n")
                        if (line!!.isEmpty()) break  // ヘッダーの終わりを検出
                    }

                    // レスポンスボディを読み取る
                    val contentLength = response.toString()
                        .lines()
                        .find { it.startsWith("Content-Length:", ignoreCase = true) }
                        ?.substringAfter(":")
                        ?.trim()
                        ?.toIntOrNull() ?: 0

                    if (contentLength > 0) {
                        val body = CharArray(contentLength)
                        reader.read(body, 0, contentLength)
                        response.append(body)
                    }

                    response.toString()
                }
            } catch (e: Exception) {
                lastException = e
                retries--
                if (retries > 0) Thread.sleep(100)  // 再試行前に少し待つ
            }
        }

        throw lastException ?: RuntimeException("Failed to send request")
    }
}