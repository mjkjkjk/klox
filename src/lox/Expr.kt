package lox

abstract class Expr {
    interface Visitor<R> {
        fun visitAssignExpr(expr: Assign): R?
        fun visitBinaryExpr(expr: Binary): R?
        fun visitCallExpr(expr: Call): R?
        fun visitGetExpr(expr: Get): R?
        fun visitGroupingExpr(expr: Grouping): R?
        fun visitLiteralExpr(expr: Literal): R?
        fun visitLogicalExpr(expr: Logical): R?
        fun visitSetExpr(expr: Set): R?
        fun visitSuperExpr(expr: Super): R?
        fun visitThisExpr(expr: This): R?
        fun visitUnaryExpr(expr: Unary): R?
        fun visitVariableExpr(expr: Variable): R?
    }

    companion object {
        class Assign(val name: Token, val value: Expr) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitAssignExpr(this)
            }
            override fun toString(): String {
                return Assign::class.simpleName.toString() + this.name.toString() + this.value.toString()
            }
        }
        class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitBinaryExpr(this)
            }
            override fun toString(): String {
                return Binary::class.simpleName.toString() + this.left.toString() + this.operator.toString() + this.right.toString()
            }
        }
        class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitCallExpr(this)
            }
            override fun toString(): String {
                return Call::class.simpleName.toString() + this.callee.toString() + this.paren.toString() + this.arguments.toString()
            }
        }
        class Get(val obj: Expr, val name: Token) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitGetExpr(this)
            }
            override fun toString(): String {
                return Get::class.simpleName.toString() + this.obj.toString() + this.name.toString()
            }
        }
        class Grouping(val expression: Expr) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitGroupingExpr(this)
            }
            override fun toString(): String {
                return Grouping::class.simpleName.toString() + this.expression.toString()
            }
        }
        class Literal(val value: Any?) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitLiteralExpr(this)
            }
            override fun toString(): String {
                return Literal::class.simpleName.toString() + this.value.toString()
            }
        }
        class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitLogicalExpr(this)
            }
            override fun toString(): String {
                return Logical::class.simpleName.toString() + this.left.toString() + this.operator.toString() + this.right.toString()
            }
        }
        class Set(val obj: Expr, val name: Token, val value: Expr) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitSetExpr(this)
            }
            override fun toString(): String {
                return Set::class.simpleName.toString() + this.obj.toString() + this.name.toString() + this.value.toString()
            }
        }
        class Super(val keyword: Token, val method: Token) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitSuperExpr(this)
            }
            override fun toString(): String {
                return Super::class.simpleName.toString() + this.keyword.toString() + this.method.toString()
            }
        }
        class This(val keyword: Token) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitThisExpr(this)
            }
            override fun toString(): String {
                return This::class.simpleName.toString() + this.keyword.toString()
            }
        }
        class Unary(val operator: Token, val right: Expr) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitUnaryExpr(this)
            }
            override fun toString(): String {
                return Unary::class.simpleName.toString() + this.operator.toString() + this.right.toString()
            }
        }
        class Variable(val name: Token) : Expr() {
            override fun <R> accept(visitor: Visitor<R>): R? {
                return visitor.visitVariableExpr(this)
            }
            override fun toString(): String {
                return Variable::class.simpleName.toString() + this.name.toString()
            }
        }
    }

    abstract fun <R> accept(visitor: Visitor<R>): R?
}
