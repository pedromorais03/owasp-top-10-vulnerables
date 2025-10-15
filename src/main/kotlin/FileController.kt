import com.sun.net.httpserver.HttpExchange
import java.io.File
import java.net.URLDecoder

object FileController {
    fun handleFile(exchange: HttpExchange) {
        val query = exchange.requestURI.query

        if (query != null) {
            val params = parseParams(query)
            val filename = params["name"]
            val operation = params["op"] ?: "read"

            if (filename != null) {
                // VULNERABILIDADE: A03:2021 - Injection (Path Traversal)
                val file = File(filename)

                // VULNERABILIDADE: A03:2021 - Injection (Command Injection)
                try {
                    when (operation) {
                        "read" -> {
                            val command = "cat $filename" // Vulnerável!
                            val process = Runtime.getRuntime().exec(command)
                            val output = process.inputStream.bufferedReader().readText()

                            println("Comando executado: $command")

                            if (file.exists() && file.isFile) {
                                val content = file.readText()

                                val response = """
                                    ==== CONTEÚDO DO ARQUIVO ====
                                    Arquivo: $filename
                                    Comando: $command
                                    Tamanho: ${file.length()} bytes
                                    Caminho absoluto: ${file.absolutePath}
                                    
                                    Conteúdo via File.readText():
                                    $content
                                    
                                    Conteúdo via Runtime.exec():
                                    $output
                                """.trimIndent()

                                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
                            }
                        }
                        "delete" -> {
                            // VULNERABILIDADE: A01:2021 - Broken Access Control
                            // Permite deletar qualquer arquivo sem autorização
                            val deleted = file.delete()
                            val response = "Arquivo $filename ${if(deleted) "deletado" else "não pode ser deletado"}"
                            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                            exchange.responseBody.use { os -> os.write(response.toByteArray()) }
                        }
                        "exec" -> {
                            // VULNERABILIDADE: A03:2021 - Command Injection (ainda mais óbvio)
                            val command = params["cmd"] ?: "ls"
                            val fullCommand = "$command $filename"
                            val process = Runtime.getRuntime().exec(fullCommand)
                            val output = process.inputStream.bufferedReader().readText()

                            val response = """
                                Comando executado: $fullCommand
                                Output:
                                $output
                            """.trimIndent()

                            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                            exchange.responseBody.use { os -> os.write(response.toByteArray()) }
                        }
                    }
                } catch (e: Exception) {
                    // VULNERABILIDADE: A05:2021 - Security Misconfiguration
                    // Stack Trace Exposure
                    val response = """
                        ❌ ERRO CRÍTICO!
                        Mensagem: ${e.message}
                        Tipo: ${e.javaClass.name}
                        Causa: ${e.cause}
                        
                        Stack Trace Completo:
                        ${e.stackTraceToString()}
                        
                        Arquivo tentado: $filename
                        Operação: $operation
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