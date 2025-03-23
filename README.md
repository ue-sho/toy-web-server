# Toy Web Server

A simple web server implementation in Kotlin.

## Running the Server

You can run either the raw server implementation or the Ktor implementation:

```bash
# Run the raw server which is implemented by myself
./gradlew run --args="raw"

# Run the web server with Ktor framework
./gradlew run --args="ktor"

# Or simply run raw server
./gradlew run
```

The server will start on port 8080 by default.

## Testing

Run the tests using:

```bash
./gradlew test
```

## Project Structure

```
app/src/main/kotlin/toy/web/server/
├── Application.kt         # Main application entry point
├── ktor/                  # Ktor server implementation
│   ├── plugins/           # Ktor plugins
│   ├── routes/            # Route definitions
│   ├── models/            # Data modelss
│   └── KtorServer.kt      # Main ktor server
└── raw/                   # Raw server implementation
    ├── core/              # Core server components
    │   ├── Server.kt      # TCP server implementation
    │   ├── Request.kt     # HTTP request parsing
    │   └── Response.kt    # HTTP response generation
    ├── routing/           # Routing system
    ├── models/            # Data models
    └── RawServer.kt       # Main raw server
```
