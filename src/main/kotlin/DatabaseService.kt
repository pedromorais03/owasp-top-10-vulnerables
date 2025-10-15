import com.sun.net.httpserver.HttpExchange
import java.net.URLDecoder

object DatabaseService {
    private val products = listOf(
        mapOf("id" to "1", "name" to "Laptop", "price" to "1000", "stock" to "10"),
        mapOf("id" to "2", "name" to "Mouse", "price" to "20", "stock" to "100"),
        mapOf("id" to "3", "name" to "Teclado", "price" to "50", "stock" to "50")
    )

    fun handleSearch(exchange: HttpExchange) {
        val query = exchange.requestURI.query

        if (query != null) {
            val params = parseParams(query)
            val searchTerm = params["q"]
            val orderBy = params["order"] ?: "name"

            if (searchTerm != null) {
                // VULNERABILIDADE: A03:2021 - Injection (SQL Injection - múltiplas)
                val sqlQuery = "SELECT * FROM products WHERE name LIKE '%$searchTerm%' ORDER BY $orderBy"
                val sqlUpdate = "UPDATE products SET views = views + 1 WHERE name = '$searchTerm'"
                val sqlUnion = "SELECT * FROM products WHERE id = $searchTerm UNION SELECT * FROM users--"

                // VULNERABILIDADE: A03:2021 - Injection (LDAP Injection)
                val ldapQuery = "(&(uid=$searchTerm)(objectClass=person))"

                // VULNERABILIDADE: A03:2021 - Injection (XPath Injection)
                val xpathQuery = "//users/user[username/text()='$searchTerm' and password/text()='anything']"

                // VULNERABILIDADE: A03:2021 - Injection (NoSQL Injection)
                val noSqlQuery = "db.products.find({ name: '$searchTerm' })"

                println("SQL Query: $sqlQuery")
                println("SQL Update: $sqlUpdate")
                println("SQL Union: $sqlUnion")
                println("LDAP Query: $ldapQuery")
                println("XPath Query: $xpathQuery")
                println("NoSQL Query: $noSqlQuery")

                val results = products.filter {
                    it["name"]?.contains(searchTerm, ignoreCase = true) == true
                }

                // VULNERABILIDADE: A04:2021 - Insecure Design
                val response = buildString {
                    appendLine("==== BUSCA EXECUTADA ====")
                    appendLine("SQL SELECT: $sqlQuery")
                    appendLine("SQL UPDATE: $sqlUpdate")
                    appendLine("SQL UNION: $sqlUnion")
                    appendLine("LDAP: $ldapQuery")
                    appendLine("XPath: $xpathQuery")
                    appendLine("NoSQL: $noSqlQuery")
                    appendLine("\nResultados (${results.size}):")
                    results.forEach { product ->
                        appendLine("- ${product["name"]} (R$ ${product["price"]}) - Stock: ${product["stock"]}")
                    }

                    // VULNERABILIDADE: A04:2021 - Insecure Design
                    // Dá dicas de como explorar vulnerabilidades
                    if (results.isEmpty()) {
                        appendLine("\n💡 Dicas de Exploit:")
                        appendLine("   SQL Injection: ' OR '1'='1")
                        appendLine("   UNION Injection: 1 UNION SELECT username,password,email FROM users--")
                        appendLine("   LDAP Injection: *)(uid=*))(|(uid=*")
                        appendLine("   XPath Injection: ' or '1'='1")
                    }
                }

                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
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