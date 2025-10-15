import com.sun.net.httpserver.HttpExchange
import java.net.URLDecoder

object UserController {

    private val userDatabase = mutableMapOf(
        "1" to mapOf("id" to "1", "name" to "Admin User", "email" to "admin@example.com", "ssn" to "123-45-6789"),
        "2" to mapOf("id" to "2", "name" to "Regular User", "email" to "user@example.com", "ssn" to "987-65-4321")
    )

    fun handleUser(exchange: HttpExchange) {
        val query = exchange.requestURI.query

        if (query != null) {
            val params = parseParams(query)
            val userId = params["id"]

            // VULNERABILIDADE: A01:2021 - Broken Access Control
            // Qualquer usuário pode acessar dados de qualquer outro usuário
            // Não há verificação de autorização
            val user = userDatabase[userId]

            if (user != null) {
                // VULNERABILIDADE: A02:2021 - Cryptographic Failures
                // Dados sensíveis (SSN) expostos sem criptografia
                val response = """
                    ID: ${user["id"]}
                    Nome: ${user["name"]}
                    Email: ${user["email"]}
                    SSN: ${user["ssn"]}
                """.trimIndent()

                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            } else {
                val response = "Usuário não encontrado"
                exchange.sendResponseHeaders(404, response.toByteArray().size.toLong())
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