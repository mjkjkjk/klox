package lib

import lox.Interpreter
import lox.LoxCallable

class Exit: LoxCallable
{
    override fun arity(): Int {
        return 0
    }

    class ExitProgram : RuntimeException(null, null, false, false)

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Double {
        throw ExitProgram()
    }

    override fun toString(): String {
        return "<native fn>"
    }
}