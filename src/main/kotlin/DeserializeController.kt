import com.sun.net.httpserver.HttpExchange
import java.io.ObjectInputStream
import java.net.URLDecoder

object DeserializeController {
    fun handleDeserialize(exchange: HttpExchange) {
        if (exchange.requestMethod == "POST") {
            try {
                // VULNERABILIDADE: A08:2021 - Software and Data Integrity Failures
                // Desserialização insegura - permite execução remota de código
                val ois = ObjectInputStream(exchange.requestBody)
                val obj = ois.readObject()

                val response = """
                    ✅ Objeto desserializado com sucesso!
                    Tipo: ${obj.javaClass.name}
                    Conteúdo: $obj
                    
                    ⚠️ PERIGO: Esta rota permite desserialização de qualquer objeto!
                    Isso pode levar a Remote Code Execution (RCE)
                """.trimIndent()

                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            } catch (e: Exception) {
                val response = """
                    Erro na desserialização: ${e.message}
                    ${e.stackTraceToString()}
                """.trimIndent()
                exchange.sendResponseHeaders(500, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            }
        }
    }
}