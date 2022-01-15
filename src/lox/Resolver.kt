package lox

import java.util.*
import kotlin.collections.HashMap

class Resolver(private val interpreter: Interpreter): Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
    private enum class FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER
    }

    private enum class ClassType {
        NONE,
        CLASS
    }

    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE
    private var currentClass = ClassType.NONE

    fun resolve(statements: List<Stmt?>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    private fun resolve(statement: Stmt?) {
        statement?.accept(this)
    }

    private fun resolve(expression: Expr) {
        expression.accept(this)
    }

    private fun resolveFunction(function: Stmt.Companion.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type

        beginScope()
        for (param in function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()

        currentFunction = enclosingFunction
    }

    private fun beginScope() {
        scopes.push(HashMap())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.empty()) return

        val scope = scopes.peek()
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Already a variable with this name in this scope.")
        }
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.empty()) return
        scopes.peek()[name.lexeme] = true
    }

    private fun resolveLocal(expression: Expr, name: Token) {
        for (i in (0 until scopes.size).reversed()) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expression, scopes.size - 1 - i)
            }
        }
    }

    override fun visitAssignExpr(expr: Expr.Companion.Assign): Unit? {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitBinaryExpr(expr: Expr.Companion.Binary): Unit? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitCallExpr(expr: Expr.Companion.Call): Unit? {
        resolve(expr.callee)
        for (argument in expr.arguments) {
            resolve(argument)
        }

        return null
    }

    override fun visitGroupingExpr(expr: Expr.Companion.Grouping): Unit? {
        resolve(expr.expression)
        return null
    }

    override fun visitLiteralExpr(expr: Expr.Companion.Literal): Unit? {
        return null
    }

    override fun visitLogicalExpr(expr: Expr.Companion.Logical): Unit? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitUnaryExpr(expr: Expr.Companion.Unary): Unit? {
        resolve(expr.right)
        return null
    }

    override fun visitVariableExpr(expr: Expr.Companion.Variable): Unit? {
        if (!scopes.empty() && scopes.peek()[expr.name.lexeme] == false) {
            Lox.error(expr.name, "Can't read local variable in its own initializer.")
        }

        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitBlockStmt(stmt: Stmt.Companion.Block): Unit? {
        beginScope()
        resolve(stmt.statements)
        endScope()
        return null
    }

    override fun visitClassStmt(stmt: Stmt.Companion.Class): Unit? {
        val enclosingClass = currentClass
        currentClass = ClassType.CLASS

        declare(stmt.name)
        define(stmt.name)

        beginScope()
        scopes.peek()["this"] = true

        for (method in stmt.methods) {
            var declaration = FunctionType.METHOD
            if (method.name.lexeme == "init") {
                declaration = FunctionType.INITIALIZER
            }
            resolveFunction(method, declaration)
        }

        endScope()

        currentClass = enclosingClass

        return null
    }

    override fun visitExpressionStmt(stmt: Stmt.Companion.Expression): Unit? {
        resolve(stmt.expression)
        return null
    }

    override fun visitFunctionStmt(stmt: Stmt.Companion.Function): Unit? {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt, FunctionType.FUNCTION)
        return null
    }

    override fun visitIfStmt(stmt: Stmt.Companion.If): Unit? {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch != null) resolve(stmt.elseBranch)
        return null
    }

    override fun visitVarStmt(stmt: Stmt.Companion.Var): Unit? {
        declare(stmt.name)
        resolve(stmt.initializer)
        define(stmt.name)
        return null
    }

    override fun visitPrintStmt(stmt: Stmt.Companion.Print): Unit? {
        resolve(stmt.expression)
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Companion.Return): Unit? {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.")
        }

        if (stmt.value != null){
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword, "Can't return a value from an initializer.")
            }

            resolve(stmt.value)
        }

        return null
    }

    override fun visitWhileStmt(stmt: Stmt.Companion.While): Unit? {
        resolve(stmt.condition)
        resolve(stmt.body)
        return null
    }

    override fun visitGetExpr(expr: Expr.Companion.Get): Unit? {
        resolve(expr.obj)
        return null
    }

    override fun visitSetExpr(expr: Expr.Companion.Set): Unit? {
        resolve(expr.value)
        resolve(expr.obj)
        return null
    }

    override fun visitThisExpr(expr: Expr.Companion.This): Unit? {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Can't use 'this' outside of a class.")
            return null
        }

        resolveLocal(expr, expr.keyword)
        return null
    }
}