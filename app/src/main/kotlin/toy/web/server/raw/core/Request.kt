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
            val lines = input.split("\r\n")

            // Parse request line
            val (method, path, version) = lines[0].split(" ")

            // Parse headers
            val headers = mutableMapOf<String, String>()
            var i = 1
            while (i < lines.size && lines[i].isNotEmpty()) {
                val (key, value) = lines[i].split(": ", limit = 2)
                headers[key.lowercase()] = value
                i++
            }

            // Parse body
            val body = if (i < lines.size - 1) {
                lines.subList(i + 1, lines.size).joinToString("\r\n")
            } else {
                ""
            }

            return Request(
                method = HttpMethod.from(method),
                path = path,
                version = version,
                headers = headers,
                body = body
            )
        }
    }
}