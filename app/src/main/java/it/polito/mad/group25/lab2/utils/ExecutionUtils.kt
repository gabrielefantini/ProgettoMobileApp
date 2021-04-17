package it.polito.mad.group25.lab2.utils

class ExecutionChain(loader: (Unit) -> Boolean) {
    private var shouldStop = false

    init {
        evaluate(loader)
    }

    fun orElse(loader: (Unit) -> Boolean): ExecutionChain {
        evaluate(loader)
        return this
    }

    private fun evaluate(loader: (Unit) -> Boolean) {
        if (!shouldStop && loader(Unit))
            shouldStop = true
    }

}