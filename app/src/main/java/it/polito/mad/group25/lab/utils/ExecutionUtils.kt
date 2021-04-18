package it.polito.mad.group25.lab.utils

class ExecutionChain(loader: () -> Boolean) {
    private var shouldStop = false

    init {
        evaluate(loader)
    }

    fun orElse(loader: () -> Boolean): ExecutionChain {
        evaluate(loader)
        return this
    }

    private fun evaluate(loader: () -> Boolean) {
        if (!shouldStop && loader())
            shouldStop = true
    }

}