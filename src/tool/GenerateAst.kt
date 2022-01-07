package tool

import java.io.PrintWriter
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory>")
        exitProcess(64)
    }

    val outputDir = args[0]

    GenerateAst().defineAst(outputDir, "Expr", listOf(
        "Assign   :: val name: Token, val value: Expr",
        "Binary   :: val left: Expr, val operator: Token, val right: Expr",
        "Call     :: val callee: Expr, val paren: Token, val arguments: List<Expr>",
        "Get      :: val obj: Expr, val name: Token",
        "Grouping :: val expression: Expr",
        "Literal  :: val value: Any?",
        "Logical  :: val left: Expr, val operator: Token, val right: Expr",
        "Set      :: val obj: Expr, val name: Token, val value: Expr",
        "Unary    :: val operator: Token, val right: Expr",
        "Variable :: val name: Token"
    ))

    GenerateAst().defineAst(outputDir, "Stmt", listOf(
        "Block      :: val statements: List<Stmt>",
        "Class      :: val name: Token, val methods: List<Stmt.Companion.Function>",
        "Expression :: val expression: Expr",
        "Function   :: val name: Token, val params: List<Token>, val body: List<Stmt>",
        "If         :: val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?",
        "Var        :: val name: Token, val initializer: Expr",
        "Print      :: val expression: Expr",
        "Return     :: val keyword: Token, val value: Expr?",
        "While      :: val condition: Expr, val body: Stmt"))
}

class GenerateAst {
    fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        val path = "$outputDir/$baseName.kt"
        val writer = PrintWriter(path, "UTF-8")

        writer.println("package lox")
        writer.println()
        writer.println("abstract class $baseName {")

        // the AST classes

        defineVisitor(writer, baseName, types)

        if (types.isNotEmpty()) {
            writer.println("    companion object {")
        }
        for (type in types) {
            val className: String = type.split("::")[0].trim()
            val fields: String = type.split("::")[1].trim()
            defineType(writer, baseName, className, fields)
        }
        if (types.isNotEmpty()) {
            writer.println("    }")
        }

        // the base accept() method
        writer.println()
        writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R?")

        writer.println("}")
        writer.close()
    }

    private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        writer.println("    interface Visitor<R> {")

        for (type in types) {
            val typeName = type.split("::")[0].trim()

            writer.println("        fun visit$typeName$baseName(${baseName.lowercase(Locale.getDefault())}: $typeName): R?")
        }

        writer.println("    }")
        writer.println()
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
        writer.println("        class $className($fieldList) : $baseName() {")
        writer.println("            override fun <R> accept(visitor: Visitor<R>): R? {")
        writer.println("                return visitor.visit$className$baseName(this)")
        writer.println("            }")
        writer.println("        }")
    }
}