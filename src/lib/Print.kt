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
            println(stringify(value))
        } else {
            println(stringify(arg))
        }
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

    override fun toString(): String {
        return "<native fn>"
    }
}