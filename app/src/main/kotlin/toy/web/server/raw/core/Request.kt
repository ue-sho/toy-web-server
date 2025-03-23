package toy.web.server.raw.core

/**
 * Represents an HTTP request method
 */
enum class HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS;

    companion object {
        fun from(method: String): HttpMethod {
            return valueOf(method.uppercase())
        }
    }
}

/**
 * Represents an HTTP request
 */
data class Request(
    val method: HttpMethod,
    val path: String,
    val version: String,
    val headers: Map<String, String>,
    val body: String
) {
    companion object {
        /**
         * Parses raw HTTP request data into a Request object
         */
        fun parse(input: String): Request {
            try {
                val lines = input.split("\r\n")
                if (lines.isEmpty()) {
                    throw IllegalArgumentException("Empty request")
                }

                // Parse request line
                val requestLineParts = lines[0].split(" ")
                if (requestLineParts.size < 3) {
                    throw IllegalArgumentException("Invalid request line: ${lines[0]}")
                }

                val method = HttpMethod.from(requestLineParts[0])
                val path = requestLineParts[1]
                val version = requestLineParts[2]

                // Parse headers
                val headers = mutableMapOf<String, String>()
                var i = 1
                while (i < lines.size && lines[i].isNotEmpty()) {
                    val headerParts = lines[i].split(": ", limit = 2)
                    if (headerParts.size == 2) {
                        headers[headerParts[0].lowercase()] = headerParts[1]
                    }
                    i++
                }

                // Parse body
                val body = if (i < lines.size - 1) {
                    lines.subList(i + 1, lines.size).joinToString("\r\n")
                } else {
                    ""
                }

                return Request(
                    method = method,
                    path = path,
                    version = version,
                    headers = headers,
                    body = body
                )
            } catch (e: Exception) {
                println("Error parsing request: ${e.message}")
                // Return a default GET request to root in case of parsing error
                return Request(
                    method = HttpMethod.GET,
                    path = "/",
                    version = "HTTP/1.1",
                    headers = mapOf(),
                    body = ""
                )
            }
        }
    }
}