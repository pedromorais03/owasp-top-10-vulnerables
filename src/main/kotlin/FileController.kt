import com.sun.net.httpserver.HttpExchange
import java.io.File
import java.net.URLDecoder

object FileController {

    fun handleFile(exchange: HttpExchange) {
        val query = exchange.requestURI.query

        if (query != null) {
            val params = parseParams(query)
            val filename = params["name"]

            if (filename != null) {
                // VULNERABILIDADE: A03:2021 - Injection (Path Traversal)
                // Não há validação do caminho do arquivo
                // Permite acesso a qualquer arquivo do sistema
                val file = File(filename)

                if (file.exists() && file.isFile) {
                    try {
                        // VULNERABILIDADE: A05:2021 - Security Misconfiguration
                        // Pode expor arquivos sensíveis do sistema
                        val content = file.readText()

                        exchange.sendResponseHeaders(200, content.toByteArray().size.toLong())
                        exchange.responseBody.use { os -> os.write(content.toByteArray()) }
                    } catch (e: Exception) {
                        // VULNERABILIDADE: A09:2021 - Security Logging and Monitoring Failures
                        // Erro detalhado exposto ao usuário
                        val response = "Erro ao ler arquivo: ${e.message}\n${e.stackTraceToString()}"
                        exchange.sendResponseHeaders(500, response.toByteArray().size.toLong())
                        exchange.responseBody.use { os -> os.write(response.toByteArray()) }
                    }
                } else {
                    val response = "Arquivo não encontrado: $filename"
                    exchange.sendResponseHeaders(404, response.toByteArray().size.toLong())
                    exchange.responseBody.use { os -> os.write(response.toByteArray()) }
                }
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