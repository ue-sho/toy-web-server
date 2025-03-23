package toy.web.server.raw.core

import java.io.File

/**
 * Represents an HTTP status code and its description
 */
enum class HttpStatus(val code: Int, val description: String) {
    OK(200, "OK"),
    CREATED(201, "Created"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
}

/**
 * Represents an HTTP response
 */
class Response(
    val status: HttpStatus = HttpStatus.OK,
    val headers: MutableMap<String, String> = mutableMapOf(),
    var body: String = ""
) {
    init {
        // Set default headers
        headers["Server"] = "ToyWebServer/1.0"
        headers["Date"] = java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME)
    }

    /**
     * Converts the response to a string that can be sent over the network
     */
    override fun toString(): String {
        // Add Content-Length header if body is not empty
        if (body.isNotEmpty()) {
            headers["Content-Length"] = body.length.toString()
        }

        val headerLines = headers.map { (key, value) -> "$key: $value" }

        return buildString {
            append("HTTP/1.1 ${status.code} ${status.description}\r\n")
            append(headerLines.joinToString("\r\n"))
            append("\r\n\r\n")
            append(body)
        }
    }

    companion object {
        /**
         * Creates a simple text response
         */
        fun text(body: String, status: HttpStatus = HttpStatus.OK): Response {
            return Response(
                headers = mutableMapOf("Content-Type" to "text/plain"),
                body = body,
                status = status
            )
        }

        /**
         * Creates a simple HTML response
         */
        fun html(body: String, status: HttpStatus = HttpStatus.OK): Response {
            return Response(
                headers = mutableMapOf("Content-Type" to "text/html"),
                body = body,
                status = status
            )
        }

        /**
         * Creates a JSON response
         */
        fun json(body: String, status: HttpStatus = HttpStatus.OK): Response {
            return Response(
                headers = mutableMapOf("Content-Type" to "application/json"),
                body = body,
                status = status
            )
        }

        fun file(file: File, contentType: String): Response {
            return Response(
                headers = mutableMapOf("Content-Type" to contentType),
                body = file.readText(),
                status = HttpStatus.OK
            )
        }
    }
}