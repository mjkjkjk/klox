package lox

class Parser(private val tokens: List<Token>) {
    private var current = 0;

    fun parse(): List<Stmt?> {
        val statements: MutableList<Stmt?> = ArrayList<Stmt?>()
        while(!isAtEnd()) {
            statements.add(declaration())
        }

        return statements
    }

    private fun expression(): Expr
    {
        return equality()
    }

    private fun declaration(): Stmt?
    {
        try {
            if (match(TokenType.VAR)) return varDeclaration()
            return statement()
        } catch(error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun statement(): Stmt
    {
        if (match(TokenType.PRINT)) return printStatement()

        return expressionStatement()
    }

    private fun printStatement(): Stmt
    {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Companion.Print(value)
    }

    private fun varDeclaration(): Stmt
    {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return initializer?.let { Stmt.Companion.Var(name, it) }!!
    }

    private fun expressionStatement(): Stmt
    {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Companion.Expression(expr)
    }

    private fun equality(): Expr {
        var expr = comparison()
        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator: Token = previous()
            val right: Expr = comparison()
            expr = Expr.Companion.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr: Expr = term()

        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right: Expr = term()
            expr = Expr.Companion.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr: Expr = factor()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator: Token = previous()
            val right: Expr = factor()
            expr = Expr.Companion.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr: Expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator: Token = previous()
            val right: Expr = factor()
            expr = Expr.Companion.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator: Token = previous()
            val right: Expr = unary()
            return Expr.Companion.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Companion.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Companion.Literal(true)
        if (match(TokenType.NIL)) return Expr.Companion.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Companion.Literal(previous().literal)
        }

        if (match(TokenType.IDENTIFIER)) {
            return Expr.Companion.Variable(previous())
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr: Expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Companion.Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) {
            return false
        }

        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) {
            current++
        }

        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS, TokenType.FUN, TokenType.IF, TokenType.PRINT,
                TokenType.RETURN, TokenType.VAR, TokenType.WHILE -> return
                else -> advance()
            }

            advance()
        }
    }

    companion object {
        private class ParseError : RuntimeException() {}
    }
}