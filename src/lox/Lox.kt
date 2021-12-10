package lox

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit {
    if (args.size > 1) {
        println("Usage: lox [script]")
        exitProcess(64)
    } else if (args.size == 1) {
        Lox.runFile(args[0])
    } else {
        Lox.runPrompt()
    }
}

class Lox {
    companion object {
        private var hadError: Boolean = false

        fun runFile(path: String): Unit {
            val bytes: ByteArray = Files.readAllBytes(Paths.get(path))
            run(String(bytes, Charset.defaultCharset()))
            if (this.hadError) exitProcess(65)
        }

        fun runPrompt() {
            while (true) {
                print("> ")
                val line: String = readLine() ?: break
                run(line)
                this.hadError = false
            }
        }

        private fun run(source: String): Unit {
            val scanner: Scanner = Scanner(source)
            val tokens: List<Token> = scanner.scanTokens()
            val parser: Parser = Parser(tokens)
            val expression: Expr? = parser.parse()

            // stop in case of syntax error
            if (hadError || expression == null) return

            println(AstPrinter().print(expression))
        }

        fun error(token: Token, message: String) {
            if (token.type == TokenType.EOF) {
                report(token.line,  " at end", message)
            } else {
                report(token.line, " at '${token.lexeme}'", message)
            }
        }

        private fun report(line: Int, where: String, message: String) {
            System.err.println("[line $line] Error$where: $message")
            this.hadError = true
        }
    }


}