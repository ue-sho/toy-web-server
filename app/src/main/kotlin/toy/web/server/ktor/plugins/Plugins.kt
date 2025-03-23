package toy.web.server.ktor.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import org.slf4j.event.Level

fun Application.configureSerialization() {
    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        json()
    }
}

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
    }
}