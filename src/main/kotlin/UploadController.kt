import com.sun.net.httpserver.HttpExchange
import java.io.File
import java.net.URLDecoder

object UploadController {
    fun handleUpload(exchange: HttpExchange) {
        if (exchange.requestMethod == "POST") {
            val query = exchange.requestURI.query
            val params = if (query != null) parseParams(query) else emptyMap()

            val filename = params["filename"] ?: "uploaded_file.txt"
            val content = exchange.requestBody.bufferedReader().readText()

            // VULNERABILIDADE: A01:2021 - Broken Access Control
            // Sem verificação de autenticação/autorização

            // VULNERABILIDADE: A04:2021 - Insecure Design
            // Sem validação de tipo de arquivo, tamanho, ou conteúdo

            // VULNERABILIDADE: A03:2021 - Injection (Path Traversal no upload)
            val file = File(filename) // Aceita qualquer caminho!

            try {
                file.writeText(content)

                // VULNERABILIDADE: A05:2021 - Security Misconfiguration
                val response = """
                    ✅ Arquivo enviado com sucesso!
                    Nome: $filename
                    Caminho: ${file.absolutePath}
                    Tamanho: ${content.length} bytes
                    
                    💡 Você pode fazer upload de:
                    - Scripts maliciosos (.sh, .php, .jsp)
                    - Webshells
                    - Arquivos executáveis
                    - Qualquer coisa, não validamos! 😈
                """.trimIndent()

                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            } catch (e: Exception) {
                val response = "Erro: ${e.message}\n${e.stackTraceToString()}"
                exchange.sendResponseHeaders(500, response.toByteArray().size.toLong())
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