package toy.web.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import toy.web.server.ktor.configureRouting
import toy.web.server.ktor.configureStatusPages
import toy.web.server.ktor.plugins.configureMonitoring
import toy.web.server.ktor.plugins.configureSerialization
import toy.web.server.raw.RawServer

/**
 * Main function that starts either the raw server or Ktor based on command line arguments
 * Usage:
 * - Raw server: ./gradlew run --args="raw"
 * - Ktor server: ./gradlew run --args="ktor"
 * - Default (Ktor): ./gradlew run
 */
fun main(args: Array<String>) {
    val serverType = args.firstOrNull()?.lowercase() ?: "ktor"
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

/**
 * Starts the raw server implementation
 */
private fun startRawServer(port: Int) {
    println("Starting raw server on port $port")
    val server = RawServer(port)
    server.start()
}

/**
 * Starts the Ktor server implementation
 */
private fun startKtorServer(port: Int) {
    println("Starting Ktor server on port $port")
    embeddedServer(Netty, port = port) {
        configureRouting()
        configureStatusPages()
        configureSerialization()
        configureMonitoring()
    }.start(wait = true)
}