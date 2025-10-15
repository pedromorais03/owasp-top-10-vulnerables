import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.io.ObjectInputStream
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.xml.parsers.DocumentBuilderFactory
import java.util.*

fun main() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)

    // Rota principal
    server.createContext("/") { exchange ->
        val response = "OWASP Top 10 Demo Server"
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        exchange.responseBody.use { os -> os.write(response.toByteArray()) }
    }

    // Rotas vulneráveis
    server.createContext("/login", AuthController::handleLogin)
    server.createContext("/user", UserController::handleUser)
    server.createContext("/file", FileController::handleFile)
    server.createContext("/search", DatabaseService::handleSearch)
    server.createContext("/execute", CommandController::handleCommand)
    server.createContext("/upload", UploadController::handleUpload)
    server.createContext("/deserialize", DeserializeController::handleDeserialize)
    server.createContext("/redirect", RedirectController::handleRedirect)
    server.createContext("/xml", XmlController::handleXml)

    println("Server started: http://localhost:8080")
    println("Simple kotlin server purposefully vulnerable for testing")
    server.start()
}