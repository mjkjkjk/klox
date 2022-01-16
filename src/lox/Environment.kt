package lox

class Environment(val enclosing: Environment? = null) {
    private val values: MutableMap<String, Any?> = HashMap()

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        if (enclosing != null) return enclosing.get(name)

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun define(name: String, value: Any?) {
        this.values[name] = value
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance)?.values?.get(name)
    }

    private fun ancestor(distance: Int): Environment? {
        var environment: Environment? = this
        for (i in 0 until distance) {
            environment = environment?.enclosing
        }

        return environment
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance)?.values?.set(name.lexeme, value)
    }
}