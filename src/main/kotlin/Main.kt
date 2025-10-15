import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

fun main() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)

    // Rota principal
    server.createContext("/") { exchange ->
        val response = "OWASP Top 10 - Demo"
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        exchange.responseBody.use { os -> os.write(response.toByteArray()) }
    }

    // Rotas vulneráveis
    server.createContext("/login", AuthController::handleLogin)
    server.createContext("/user", UserController::handleUser)
    server.createContext("/file", FileController::handleFile)
    server.createContext("/search", DatabaseService::handleSearch)

    println("Servidor iniciado em http://localhost:8080")
    println("⚠️ Servidor de teste do OWASP Top 10")
    server.start()
}