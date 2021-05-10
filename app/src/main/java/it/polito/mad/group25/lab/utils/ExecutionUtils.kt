package it.polito.mad.group25.lab.utils

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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


/**
 * Class which purpose is to handle async update of a variable and waiting for it to be ready.
 */
class AsyncValueNotReadyException(waitedMillis: Long) :
    Exception("Async value was not ready after $waitedMillis ms")

class AsyncStore<T>(private val maxMillis: Long = 15000) {

    private val lock = ReentrantLock()
    private val isReady = lock.newCondition()

    var stored: T? = null
        get() {
            lock.withLock {
                if (!isReady.await(maxMillis, TimeUnit.MILLISECONDS))
                    throw AsyncValueNotReadyException(maxMillis)
            }
            return field
        }
        set(value) {
            lock.withLock {
                field = value
                isReady.signal()
            }
        }

}
