import com.sun.net.httpserver.HttpExchange
import java.net.URLDecoder
import javax.xml.parsers.DocumentBuilderFactory

object XmlController {
    fun handleXml(exchange: HttpExchange) {
        if (exchange.requestMethod == "POST") {
            try {
                val xmlContent = exchange.requestBody.bufferedReader().readText()

                // VULNERABILIDADE: A05:2021 - Security Misconfiguration (XXE)
                // XML External Entity - não desabilita entidades externas
                val factory = DocumentBuilderFactory.newInstance()
                // NÃO configura proteções contra XXE!
                // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)

                val builder = factory.newDocumentBuilder()
                val document = builder.parse(xmlContent.byteInputStream())

                val response = """
                    ✅ XML processado com sucesso!
                    Root Element: ${document.documentElement.nodeName}
                    
                    ⚠️ VULNERABILIDADE XXE!
                    Você pode ler arquivos do sistema usando:
                    <?xml version="1.0"?>
                    <!DOCTYPE foo [
                      <!ENTITY xxe SYSTEM "file:///etc/passwd">
                    ]>
                    <root>&xxe;</root>
                """.trimIndent()

                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            } catch (e: Exception) {
                val response = "Erro XML: ${e.message}\n${e.stackTraceToString()}"
                exchange.sendResponseHeaders(500, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            }
        }
    }
}