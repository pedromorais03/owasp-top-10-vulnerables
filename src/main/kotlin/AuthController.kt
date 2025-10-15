import com.sun.net.httpserver.HttpExchange
import java.net.URLDecoder

object AuthController {

    // VULNERABILIDADE: A01:2021 - Broken Access Control
    // Não há validação de sessão adequada
    private val users = mutableMapOf(
        "admin" to "admin123",  // Senha fraca hardcoded
        "user" to "password"
    )

    fun handleLogin(exchange: HttpExchange) {
        if (exchange.requestMethod == "POST") {
            val requestBody = exchange.requestBody.bufferedReader().readText()
            val params = parseParams(requestBody)

            val username = params["username"]
            val password = params["password"]

            // VULNERABILIDADE: A07:2021 - Identification and Authentication Failures
            // Sem rate limiting, permite brute force
            if (users[username] == password) {
                // VULNERABILIDADE: A02:2021 - Cryptographic Failures
                // Token previsível e não criptografado
                val token = "$username:${System.currentTimeMillis()}"

                val response = """
                    Login bem-sucedido!
                    Token: $token
                    Role: ${if(username == "admin") "admin" else "user"}
                """.trimIndent()

                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            } else {
                // VULNERABILIDADE: A09:2021 - Security Logging and Monitoring Failures
                // Não há log de tentativas de login falhas
                val response = "Login falhou para usuário: $username"
                exchange.sendResponseHeaders(401, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            }
        }
    }

    private fun parseParams(query: String): Map<String, String> {
        return query.split("&").associate {
            val (key, value) = it.split("=")
            key to URLDecoder.decode(value, "UTF-8")
        }
    }
}