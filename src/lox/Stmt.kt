package lox

abstract class Stmt {
    interface Visitor<R> {
        fun visitBlockStmt(stmt: Block): R?
        fun visitClassStmt(stmt: Class): R?
        fun visitExpressionStmt(stmt: Expression): R?
        fun visitFunctionStmt(stmt: Function): R?
        fun visitIfStmt(stmt: If): R?
        fun visitVarStmt(stmt: Var): R?
        fun visitReturnStmt(stmt: Return): R?
        fun visitWhileStmt(stmt: While): R?
    }

    companion object {
        class Block(val statements: List<Stmt>) : Stmt() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitBlockStmt(this)
            }
            override fun toString(): String {
                return Block::class.simpleName.toString() + this.statements.toString()
            }
        }
        class Class(val name: Token, val superclass: Expr.Companion.Variable?, val methods: List<Function>) : Stmt() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitClassStmt(this)
            }
            override fun toString(): String {
                return Class::class.simpleName.toString() + this.name.toString() + this.superclass.toString() + this.methods.toString()
            }
        }
        class Expression(val expression: Expr) : Stmt() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitExpressionStmt(this)
            }
            override fun toString(): String {
                return Expression::class.simpleName.toString() + this.expression.toString()
            }
        }
        class Function(val name: Token, val params: List<Token>, val body: List<Stmt>) : Stmt() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitFunctionStmt(this)
            }
            override fun toString(): String {
                return Function::class.simpleName.toString() + this.name.toString() + this.params.toString() + this.body.toString()
            }
        }
        class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitIfStmt(this)
            }
            override fun toString(): String {
                return If::class.simpleName.toString() + this.condition.toString() + this.thenBranch.toString() + this.elseBranch.toString()
            }
        }
        class Var(val name: Token, val initializer: Expr) : Stmt() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitVarStmt(this)
            }
            override fun toString(): String {
                return Var::class.simpleName.toString() + this.name.toString() + this.initializer.toString()
            }
        }
        class Return(val keyword: Token, val value: Expr?) : Stmt() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitReturnStmt(this)
            }
            override fun toString(): String {
                return Return::class.simpleName.toString() + this.keyword.toString() + this.value.toString()
            }
        }
        class While(val condition: Expr, val body: Stmt) : Stmt() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitWhileStmt(this)
            }
            override fun toString(): String {
                return While::class.simpleName.toString() + this.condition.toString() + this.body.toString()
            }
        }
    }

    abstract fun <R> accept(visitor: Visitor<R>): R?
}
