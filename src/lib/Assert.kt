package lib

import lox.*

class Assert: LoxCallable
{
    override fun arity(): Int {
        return 1
    }

    class AssertFailed : RuntimeException(null, null, false, false)

    override fun call(interpreter: Interpreter, arguments: List<Any?>) {
        val arg = arguments[0]
        if (!interpreter.isTruthy(arg)) {
            throw AssertFailed()
        }
    }

    override fun toString(): String {
        return "<native fn>"
    }
}