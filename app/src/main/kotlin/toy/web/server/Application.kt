package toy.web.server

import toy.web.server.ktor.startKtorServer
import toy.web.server.raw.startRawServer

/**
 * Main function that starts either the raw server or Ktor based on command line arguments
 * Usage:
 * - Raw server: ./gradlew run --args="raw"
 * - Ktor server: ./gradlew run --args="ktor"
 * - Default (Ktor): ./gradlew run
 */
fun main(args: Array<String>) {
    val serverType = args.firstOrNull()?.lowercase() ?: "raw"
    val port = 8080

    when (serverType) {
        "raw" -> startRawServer(port)
        "ktor" -> startKtorServer(port)
        else -> {
            println("Unknown server type: $serverType")
            println("Usage: ./gradlew run --args=\"[raw|ktor]\"")
            return
        }
    }
}
