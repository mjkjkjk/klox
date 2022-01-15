package lox

fun main(args: Array<String>) {
    val expression: Expr = Expr.Companion.Binary(
        Expr.Companion.Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Expr.Companion.Literal(123)
        ),
        Token(TokenType.STAR, "*", null, 1),
        Expr.Companion.Grouping(
            Expr.Companion.Literal(45.67)
        )
    )

    println(AstPrinter().print(expression))
}

class AstPrinter : Expr.Visitor<String> {
    fun print(expr: Expr): String? {
        return expr.accept(this)
    }

    override fun visitBinaryExpr(expr: Expr.Companion.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitGroupingExpr(expr: Expr.Companion.Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Companion.Literal): String {
        if (expr.value == null) return "nil"
        return expr.value.toString()
    }

    override fun visitUnaryExpr(expr: Expr.Companion.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    private fun parenthesize(name: String, vararg expressions: Expr): String {
        val builder = StringBuilder()

        builder.append("(").append(name)
        for (expr in expressions) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")

        return builder.toString()
    }

    override fun visitVariableExpr(expr: Expr.Companion.Variable): String? {
        return "Defined: ${expr.name}"
    }

    override fun visitAssignExpr(expr: Expr.Companion.Assign): String? {
        return "Assigned: ${expr.name} as: ${expr.value}"
    }

    override fun visitLogicalExpr(expr: Expr.Companion.Logical): String? {
        TODO("Not yet implemented")
    }

    override fun visitCallExpr(expr: Expr.Companion.Call): String? {
        TODO("Not yet implemented")
    }

    override fun visitGetExpr(expr: Expr.Companion.Get): String? {
        TODO("Not yet implemented")
    }

    override fun visitSetExpr(expr: Expr.Companion.Set): String? {
        TODO("Not yet implemented")
    }

    override fun visitThisExpr(expr: Expr.Companion.This): String? {
        TODO("Not yet implemented")
    }
}