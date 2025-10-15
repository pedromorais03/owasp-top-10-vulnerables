import com.sun.net.httpserver.HttpExchange
import java.net.URLDecoder

object CommandController {
    fun handleCommand(exchange: HttpExchange) {
        val query = exchange.requestURI.query

        if (query != null) {
            val params = parseParams(query)
            val cmd = params["cmd"]
            val args = params["args"]

            if (cmd != null) {
                try {
                    // VULNERABILIDADE: A03:2021 - Injection (Command Injection)
                    val fullCommand = if (args != null) "$cmd $args" else cmd
                    val process = Runtime.getRuntime().exec(fullCommand)
                    val output = process.inputStream.bufferedReader().readText()
                    val errors = process.errorStream.bufferedReader().readText()

                    // VULNERABILIDADE: A09:2021 - Security Logging and Monitoring Failures
                    // Não há log adequado de comandos perigosos executados
                    println("Executando comando: $fullCommand")

                    val response = """
                        ==== COMANDO EXECUTADO ====
                        Comando: $fullCommand
                        Exit Code: ${process.waitFor()}
                        
                        📤 Output:
                        $output
                        
                        ⚠️ Errors:
                        $errors
                        
                        💡 Exemplos de comandos perigosos:
                        - whoami
                        - cat /etc/passwd
                        - ls -la /
                        - rm -rf (NÃO FAÇA ISSO!)
                    """.trimIndent()

                    exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                    exchange.responseBody.use { os -> os.write(response.toByteArray()) }
                } catch (e: Exception) {
                    // VULNERABILIDADE: A05:2021 - Security Misconfiguration
                    val response = """
                        ❌ ERRO ao executar comando '$cmd':
                        ${e.message}
                        
                        Stack Trace:
                        ${e.stackTraceToString()}
                    """.trimIndent()
                    exchange.sendResponseHeaders(500, response.toByteArray().size.toLong())
                    exchange.responseBody.use { os -> os.write(response.toByteArray()) }
                }
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