package it.polito.mad.group25.lab.utils.persistence.observers

import it.polito.mad.group25.lab.utils.persistence.LiveDataPersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.PersistorAware
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistenceObserver

abstract class ChainedObserver<T>(
    private val next: PersistenceObserver<T> = object : PersistenceObserver<T> {}
) :
    PersistenceObserver<T> by next,
    LiveDataPersistenceObserver<T>, FirestoreLivePersistenceObserver<Any?, T>,
    PersistorAware<T, Any?, SimplePersistor<T, Any?>> {

    override fun setPersistor(persistor: SimplePersistor<T, Any?>) {
        if (next is PersistorAware<*, *, *>) {
            next as PersistorAware<T, Any?, SimplePersistor<T, Any?>>
            next.setPersistor(persistor)
        }
    }

    override fun onAsyncValueReceived(value: Any?, error: Exception?) {
        if (next is FirestoreLivePersistenceObserver<*, *>) {
            next as FirestoreLivePersistenceObserver<Any?, T>
            next.onAsyncValueReceived(value, error)
        }
    }

    override fun onLiveValueChanges(newValue: T) {
        if (next is LiveDataPersistenceObserver<*>) {
            next as LiveDataPersistenceObserver<T>
            next.onLiveValueChanges(newValue)
        }
    }

    override fun afterPerformingLiveValuePersistency(value: T, ex: Exception?) {
        if (next is LiveDataPersistenceObserver<*>) {
            next as LiveDataPersistenceObserver<T>
            next.afterPerformingLiveValuePersistency(value, ex)
        }
    }

    override fun beforePerformingLiveValuePersistency(value: T): T? {
        if (next is LiveDataPersistenceObserver<*>) {
            next as LiveDataPersistenceObserver<T>
            return next.beforePerformingLiveValuePersistency(value)
        }
        return value
    }

    companion object Builder {

        class ChainBuilder<T> {

            val chain: MutableList<PersistenceObserver<T>> = mutableListOf()

            fun wrappedBy(next: (PersistenceObserver<T>) -> ChainedObserver<T>): ChainBuilder<T> {
                chain.add(next(chain.last()))
                return this
            }

            fun build(): PersistenceObserver<T> = chain.last()

        }

        fun <T> startingFrom(first: PersistenceObserver<T>): ChainBuilder<T> {
            return ChainBuilder<T>().apply { chain.add(first) }
        }
    }
}
