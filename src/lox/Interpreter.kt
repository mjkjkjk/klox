package lox

import lib.Clock
import lib.Exit

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private val globals = Environment()
    private var environment: Environment = globals
    private val locals = HashMap<Expr, Int>()

    init {
        globals.define("clock", Clock())
        globals.define("exit", Exit())
    }

    fun interpret(statements: List<Stmt?>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: Exit.ExitProgram) {
            Lox.runtimePrint(">>> exit() called, stopping execution")
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
            else -> return null
        }
    }

    override fun visitCallExpr(expr: Expr.Companion.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = ArrayList<Any?>()
        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }

        val function: LoxCallable = callee

        if (arguments.size != function.arity()) {
            throw RuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}.")
        }

        return function.call(this, arguments)
    }

    override fun visitGroupingExpr(expr: Expr.Companion.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Companion.Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Expr.Companion.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -right.toString().toDouble()
            }
            else -> null
        }
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

    private fun execute(statement: Stmt?)
    {
        statement?.accept(this)
    }

    fun executeBlock(statements: List<Stmt?>, environment: Environment)
    {
        val previous = this.environment

        try {
            this.environment = environment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
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
        return lookUpVariable(expr.name, expr)
    }

    private fun lookUpVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }

    override fun visitVarStmt(stmt: Stmt.Companion.Var): Unit? {
        val value: Any? = evaluate(stmt.initializer)

        environment.define(stmt.name.lexeme, value)
        return null
    }

    override fun visitAssignExpr(expr: Expr.Companion.Assign): Any? {
        val value = evaluate(expr.value)

        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }

        return value
    }

    override fun visitBlockStmt(stmt: Stmt.Companion.Block): Unit? {
        executeBlock(stmt.statements, Environment(environment))
        return null
    }

    override fun visitIfStmt(stmt: Stmt.Companion.If): Unit? {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }

        return null
    }

    override fun visitLogicalExpr(expr: Expr.Companion.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitWhileStmt(stmt: Stmt.Companion.While): Unit? {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
        return null
    }

    override fun visitFunctionStmt(stmt: Stmt.Companion.Function): Unit? {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Companion.Return): Unit? {
        var value: Any? = null
        if (stmt.value != null) {
            value = evaluate(stmt.value)
        }

        throw Return(value)
    }

    fun resolve(expression: Expr, depth: Int) {
        locals[expression] = depth
    }

    override fun visitClassStmt(stmt: Stmt.Companion.Class): Unit? {
        var superclass: Any? = null
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass)
            if (superclass !is LoxClass) {
                throw RuntimeError(stmt.superclass.name, "Superclass must be a class.")
            }
        }

        environment.define(stmt.name.lexeme, null)

        val methods = HashMap<String, LoxFunction>()
        for (method in stmt.methods) {
            val function = LoxFunction(method, environment, method.name.lexeme == "this")
            methods[method.name.lexeme] = function
        }

        val klass = LoxClass(stmt.name.lexeme, superclass as LoxClass?, methods)

        environment.assign(stmt.name, klass)
        return null
    }

    override fun visitGetExpr(expr: Expr.Companion.Get): Any? {
        val obj = evaluate(expr.obj)

        if (obj is LoxInstance) {
            return obj.get(expr.name)
        }

        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitSetExpr(expr: Expr.Companion.Set): Any? {
        val obj = evaluate(expr.obj)

        if (obj !is LoxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields.")
        }

        val value = evaluate(expr.value)
        obj.set(expr.name, value)
        return value
    }

    override fun visitThisExpr(expr: Expr.Companion.This): Any? {
        return lookUpVariable(expr.keyword, expr)
    }
}