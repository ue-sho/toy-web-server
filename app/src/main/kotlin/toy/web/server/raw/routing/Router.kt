package toy.web.server.raw.routing

import toy.web.server.raw.core.Request
import toy.web.server.raw.core.Response
import toy.web.server.raw.core.HttpMethod
import toy.web.server.raw.core.HttpStatus

/**
 * Represents a route handler function
 */
typealias Handler = (Request) -> Response

/**
 * Represents a route with its path pattern and handler
 */
data class Route(
    val method: HttpMethod,
    val pattern: String,
    val handler: Handler
)

/**
 * Router class for handling HTTP request routing
 */
class Router {
    private val routes = mutableListOf<Route>()

    /**
     * Adds a new route for GET requests
     */
    fun get(path: String, handler: Handler) {
        routes.add(Route(HttpMethod.GET, path, handler))
    }

    /**
     * Adds a new route for POST requests
     */
    fun post(path: String, handler: Handler) {
        routes.add(Route(HttpMethod.POST, path, handler))
    }

    /**
     * Adds a new route for PUT requests
     */
    fun put(path: String, handler: Handler) {
        routes.add(Route(HttpMethod.PUT, path, handler))
    }

    /**
     * Adds a new route for DELETE requests
     */
    fun delete(path: String, handler: Handler) {
        routes.add(Route(HttpMethod.DELETE, path, handler))
    }

    /**
     * Handles an incoming request by finding and executing the appropriate route handler
     */
    fun handle(request: Request): Response {
        val route = routes.find { route ->
            route.method == request.method && pathMatches(route.pattern, request.path)
        }

        return route?.handler?.invoke(request) ?: Response(
            status = HttpStatus.NOT_FOUND,
            body = "404 Not Found"
        )
    }

    /**
     * Checks if a request path matches a route pattern
     * Currently supports exact matches and simple wildcard patterns
     */
    private fun pathMatches(pattern: String, path: String): Boolean {
        // TODO: Implement more sophisticated path matching with parameters
        return when {
            pattern == path -> true
            pattern.endsWith("/*") && path.startsWith(pattern.removeSuffix("/*")) -> true
            else -> false
        }
    }
}