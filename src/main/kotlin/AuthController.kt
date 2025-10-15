import com.sun.net.httpserver.HttpExchange
import java.net.URLDecoder
import java.security.MessageDigest
import javax.crypto.Cipher
import kotlin.random.Random

object AuthController {
    // VULNERABILIDADE: A02:2021 - Cryptographic Failures
    // Hardcoded Credentials - SAST detecta FACILMENTE
    private const val API_KEY = "sk-1234567890abcdef-SUPER-SECRET-KEY"
    private const val DB_PASSWORD = "SuperSecret123!"
    private const val AWS_SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
    private const val JWT_SECRET = "my-super-secret-jwt-key-123456"
    private const val PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQ..."

    private val users = mutableMapOf(
        "admin" to "admin123",  // Senha fraca
        "user" to "password",
        "root" to "toor",
        "test" to "test"
    )

    // VULNERABILIDADE: A07:2021 - Identification and Authentication Failures
    private var loginAttempts = 0 // Sem limite de tentativas

    fun handleLogin(exchange: HttpExchange) {
        if (exchange.requestMethod == "POST") {
            val requestBody = exchange.requestBody.bufferedReader().readText()
            val params = parseParams(requestBody)

            val username = params["username"]
            val password = params["password"]
            val remember = params["remember"] // Usado para sessão persistente

            // VULNERABILIDADE: A03:2021 - Injection (SQL Injection)
            val sqlQuery = "SELECT * FROM users WHERE username='$username' AND password='$password'"
            println("Executando SQL: $sqlQuery")

            // VULNERABILIDADE: A02:2021 - Cryptographic Failures (Weak Crypto)
            val md5 = MessageDigest.getInstance("MD5")
            val sha1 = MessageDigest.getInstance("SHA-1") // Também fraco
            val hashedPassword = md5.digest(password?.toByteArray()).toString()

            // VULNERABILIDADE: A02:2021 - Cryptographic Failures (DES)
            val desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding")

            // VULNERABILIDADE: A07:2021 - Authentication Failures (No rate limiting)
            loginAttempts++
            println("Tentativa de login #$loginAttempts para usuário: $username")

            if (users[username] == password) {
                // VULNERABILIDADE: A02:2021 - Cryptographic Failures (Predictable Random)
                val random = Random(System.currentTimeMillis()) // Seed previsível
                val sessionId = random.nextInt().toString()

                // VULNERABILIDADE: A02:2021 - Weak Token Generation
                val token = "$username:${System.currentTimeMillis()}:$sessionId"

                // VULNERABILIDADE: A05:2021 - Security Misconfiguration
                // Expõe informações sensíveis na resposta
                val response = """
                    ✅ Login bem-sucedido!
                    Token: $token
                    API Key: $API_KEY
                    DB Password: $DB_PASSWORD
                    AWS Secret: $AWS_SECRET
                    Role: ${if(username == "admin") "admin" else "user"}
                    Session: $sessionId
                    MD5 Hash: $hashedPassword
                    Remember Me: $remember
                    Total Login Attempts: $loginAttempts
                """.trimIndent()

                // VULNERABILIDADE: A05:2021 - Security Misconfiguration
                // Headers inseguros
                exchange.responseHeaders.add("X-Powered-By", "Kotlin/1.9.0")
                exchange.responseHeaders.add("Server", "VulnerableServer/1.0")

                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            } else {
                // VULNERABILIDADE: A04:2021 - Insecure Design
                // Information Disclosure - revela se usuário existe
                val response = if (users.containsKey(username)) {
                    "❌ Login falhou. Senha incorreta para usuário '$username'. SQL: $sqlQuery"
                } else {
                    "❌ Login falhou. Usuário '$username' não existe. SQL: $sqlQuery"
                }

                exchange.sendResponseHeaders(401, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            }
        }
    }

    private fun parseParams(query: String): Map<String, String> {
        return query.split("&").associate {
            val parts = it.split("=")
            if (parts.size == 2) {
                parts[0] to URLDecoder.decode(parts[1], "UTF-8")
            } else {
                parts[0] to ""
            }
        }
    }
}