package lib

import lox.Interpreter
import lox.LoxCallable

class Clock: LoxCallable
{
    override fun arity(): Int {
        return 0
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Double {
        return System.currentTimeMillis().toDouble() / 1000.0
    }

    override fun toString(): String {
        return "<native fn>"
    }
}