package lox

import lox.TokenType.*

class Scanner(private val source: String) {
    private val tokens: ArrayList<Token> = ArrayList()
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1

    companion object {
        private val keywords: Map<String, TokenType> = hashMapOf(
            "and" to AND,
            "class" to CLASS,
            "else" to ELSE,
            "false" to FALSE,
            "for" to FOR,
            "fun" to FUN,
            "if" to IF,
            "nil" to NIL,
            "or" to OR,
            "print" to PRINT,
            "return" to RETURN,
            "super" to SUPER,
            "this" to THIS,
            "true" to TRUE,
            "var" to VAR,
            "while" to WHILE
        )

    }

    fun scanTokens(): List<Token> {
        while(!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken(): Unit {
        when (val c: Char = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                 } else {
                     addToken(SLASH)
                }
            }

            ' ', '\r', '\t' -> Unit
            '\n' -> line++

            '"' -> string()
            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)){
                    identifier()
                } else {
                    Lox.error(Token(TokenType.STRING, "", "", line), "Unexpected character.")
                }
            }
        }
    }

    private fun identifier(): Unit {
        while (isAlphaNumeric(peek())) {
            advance()
        }

        val text: String = source.substring(start, current)
        var type: TokenType? = keywords.get(text)
        if (type == null) {
            type = IDENTIFIER
        }
        addToken(type)
    }

    private fun number(): Unit {
        while(isDigit(peek())) advance()

        // look for fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            advance()

            while(isDigit(peek())) advance()
        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun string(): Unit {
        while(peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(Token(TokenType.STRING, "", "", line), "Unterminated string.")
            return
        }

        advance() // closing "

        // trim the quotes
        val value: String = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) {
            return false
        }
        if (source[current] != expected) {
            return false
        }

        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0003'
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0003'
        return source[current + 1]
    }

    private fun isAlpha(c: Char): Boolean {
        return (c in ('a'..'z')) || (c in ('A'..'Z')) || c == '_'
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun addToken(type: TokenType): Unit {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?): Unit
    {
        val text: String = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
}