package lox

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    fun interpret(statements: List<Stmt?>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    override fun visitBinaryExpr(expr: Expr.Companion.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                return left.toString().toDouble() > right.toString().toDouble()
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left.toString().toDouble() >= right.toString().toDouble()
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                return left.toString().toDouble() <= right.toString().toDouble()
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left.toString().toDouble() <= right.toString().toDouble()
            }
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return left.toString().toDouble() - right.toString().toDouble()
            }
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    return left.toString().toDouble() + right.toString().toDouble()
                }

                if (left is String && right is String) {
                    return left.toString() + right.toString()
                }

                throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                return left.toString().toDouble() / right.toString().toDouble()
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return left.toString().toDouble() * right.toString().toDouble()
            }
            TokenType.BANG_EQUAL -> return !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> return isEqual(left, right)
        }

        return null
    }

    override fun visitGroupingExpr(expr: Expr.Companion.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Companion.Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Expr.Companion.Unary): Any? {
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.BANG -> return !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                return -right.toString().toDouble()
            }
        }

        return null
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return

        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun execute(statement: Stmt?): Unit
    {
        statement?.accept(this)
    }

    override fun visitExpressionStmt(stmt: Stmt.Companion.Expression): Unit? {
        evaluate(stmt.expression)
        return null
    }

    override fun visitPrintStmt(stmt: Stmt.Companion.Print): Unit? {
        val value = evaluate(stmt.expression)
        println(stringify(value))
        return null
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean{
        if (a == null && b == null) return true
        if (a == null) return false

        return a == b
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        return obj.toString()
    }

    override fun visitVariableExpr(expr: Expr.Companion.Variable): Any? {
        TODO("Not yet implemented")
    }

    override fun visitVarStmt(stmt: Stmt.Companion.Var): Unit? {
        TODO("Not yet implemented")
    }
}