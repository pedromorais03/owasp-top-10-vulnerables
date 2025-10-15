import com.sun.net.httpserver.HttpExchange
import java.net.URLDecoder

object UserController {
    // VULNERABILIDADE: A02:2021 - Cryptographic Failures (Sensitive Data Storage)
    private val userDatabase = mutableMapOf(
        "1" to mapOf(
            "id" to "1",
            "name" to "Admin User",
            "email" to "admin@example.com",
            "ssn" to "123-45-6789",
            "credit_card" to "4532-1234-5678-9010",
            "cvv" to "123",
            "password" to "admin123",
            "api_key" to "sk-proj-1234567890",
            "birth_date" to "1990-01-01",
            "salary" to "150000",
            "bank_account" to "12345-6"
        ),
        "2" to mapOf(
            "id" to "2",
            "name" to "Regular User",
            "email" to "user@example.com",
            "ssn" to "987-65-4321",
            "credit_card" to "5425-2334-3010-9876",
            "cvv" to "456",
            "password" to "password123",
            "api_key" to "sk-proj-0987654321",
            "birth_date" to "1995-05-15",
            "salary" to "50000",
            "bank_account" to "98765-4"
        )
    )

    fun handleUser(exchange: HttpExchange) {
        val query = exchange.requestURI.query

        if (query != null) {
            val params = parseParams(query)
            val userId = params["id"]
            val action = params["action"]

            // VULNERABILIDADE: A03:2021 - Injection (SQL Injection)
            val sqlQuery = "SELECT * FROM users WHERE id = $userId"
            println("Query SQL: $sqlQuery")

            // VULNERABILIDADE: A01:2021 - Broken Access Control
            // Qualquer usuário pode acessar qualquer perfil, sem verificação de autorização
            val user = userDatabase[userId]

            if (user != null) {
                // VULNERABILIDADE: A02:2021 - Cryptographic Failures
                // Sensitive Data Exposure - expõe TODOS os dados sensíveis
                val response = """
                    ==== DADOS DO USUÁRIO ====
                    ID: ${user["id"]}
                    Nome: ${user["name"]}
                    Email: ${user["email"]}
                    SSN: ${user["ssn"]}
                    Credit Card: ${user["credit_card"]}
                    CVV: ${user["cvv"]}
                    Password: ${user["password"]}
                    API Key: ${user["api_key"]}
                    Birth Date: ${user["birth_date"]}
                    Salary: $${user["salary"]}
                    Bank Account: ${user["bank_account"]}
                    AWS Secret: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
                    
                    SQL Executado: $sqlQuery
                    Action: $action
                """.trimIndent()

                // VULNERABILIDADE: A05:2021 - Security Misconfiguration
                // CORS mal configurado - permite qualquer origem
                exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
                exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                exchange.responseHeaders.add("Access-Control-Allow-Headers", "*")

                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            } else {
                val response = "Usuário não encontrado. Query executada: $sqlQuery"
                exchange.sendResponseHeaders(404, response.toByteArray().size.toLong())
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