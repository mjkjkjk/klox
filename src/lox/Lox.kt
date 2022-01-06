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
        private val interpreter: Interpreter = Interpreter()
        private var hadError: Boolean = false
        private var hadRuntimeError: Boolean = false

        fun runFile(path: String): Unit {
            val bytes: ByteArray = Files.readAllBytes(Paths.get(path))
            run(String(bytes, Charset.defaultCharset()))
            if (this.hadError) exitProcess(65)
            if (this.hadRuntimeError) exitProcess(70)
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
            val statements = parser.parse()

            // stop in case of syntax error
            if (hadError) return

            val resolver = Resolver(interpreter)
            resolver.resolve(statements)

            interpreter.interpret(statements)
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

        fun runtimeError(error: RuntimeError) {
            System.err.println("${error.message}\n[line ${error.token.line}]")
            this.hadRuntimeError = true
        }
    }
}