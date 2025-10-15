import com.sun.net.httpserver.HttpExchange
import java.net.URLDecoder

object DatabaseService {

    // Simulação de banco de dados
    private val products = listOf(
        mapOf("id" to "1", "name" to "Laptop", "price" to "1000"),
        mapOf("id" to "2", "name" to "Mouse", "price" to "20"),
        mapOf("id" to "3", "name" to "Teclado", "price" to "50")
    )

    fun handleSearch(exchange: HttpExchange) {
        val query = exchange.requestURI.query

        if (query != null) {
            val params = parseParams(query)
            val searchTerm = params["q"]

            if (searchTerm != null) {
                // VULNERABILIDADE: A03:2021 - Injection (SQL Injection simulado)
                // Construção insegura de query sem sanitização
                val sqlQuery = "SELECT * FROM products WHERE name LIKE '%$searchTerm%'"

                // VULNERABILIDADE: A06:2021 - Vulnerable and Outdated Components
                // Comentário indicando uso de biblioteca desatualizada
                println("Executando query: $sqlQuery")
                println("// Usando biblioteca fictícia vulnerable-db v1.0.0 (vulnerável)")

                // Simulação de busca
                val results = products.filter {
                    it["name"]?.contains(searchTerm, ignoreCase = true) == true
                }

                val response = buildString {
                    appendLine("Query executada: $sqlQuery")
                    appendLine("\nResultados:")
                    results.forEach { product ->
                        appendLine("- ${product["name"]} (R$ ${product["price"]})")
                    }

                    // VULNERABILIDADE: A04:2021 - Insecure Design
                    // Expõe estrutura interna do banco de dados
                    if (results.isEmpty()) {
                        appendLine("\nDica: Tente usar: ' OR '1'='1")
                    }
                }

                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
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