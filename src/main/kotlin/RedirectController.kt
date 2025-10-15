import com.sun.net.httpserver.HttpExchange
import java.net.URLDecoder

object RedirectController {
    fun handleRedirect(exchange: HttpExchange) {
        val query = exchange.requestURI.query

        if (query != null) {
            val params = parseParams(query)
            val url = params["url"]

            if (url != null) {
                // VULNERABILIDADE: A01:2021 - Broken Access Control (Open Redirect)
                // Redireciona para qualquer URL sem validação
                exchange.responseHeaders.add("Location", url)

                val response = """
                    Redirecionando para: $url
                    
                    💡 Exploit: Pode ser usado para phishing!
                    Exemplo: /redirect?url=http://malicious-site.com
                """.trimIndent()

                exchange.sendResponseHeaders(302, response.toByteArray().size.toLong())
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