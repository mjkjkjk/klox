package lib

import lox.Expr
import lox.Interpreter
import lox.LoxCallable

class Print: LoxCallable
{
    override fun arity(): Int {
        return 1
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>) {
        val arg = arguments[0]
        if (arg !is String && arg !is Double && arg !is Int) {
            val value = interpreter.evaluate(arg as Expr)
            println(interpreter.stringify(value))
        } else {
            println(arg)
        }
    }

    override fun toString(): String {
        return "<native fn>"
    }
}