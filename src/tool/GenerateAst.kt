package tool

import java.io.PrintWriter
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory>");
        exitProcess(64)
    }

    val outputDir = args[0]

    GenerateAst().defineAst(outputDir, "Expr", listOf<String>(
        "Binary   :: val left: Expr, val operator: Token, val right: Expr",
        "Grouping :: val expression: Expr",
        "Literal  :: val value: Any?",
        "Unary    :: val operator: Token, val right: Expr"
    ))
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