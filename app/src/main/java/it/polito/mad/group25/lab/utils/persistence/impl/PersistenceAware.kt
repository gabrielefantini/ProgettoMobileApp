package it.polito.mad.group25.lab.utils.persistence.impl

import com.fasterxml.jackson.annotation.JsonIgnore
import it.polito.mad.group25.lab.utils.BidirectionalMapper
import it.polito.mad.group25.lab.utils.persistence.DelegatingPersistencyObserver
import it.polito.mad.group25.lab.utils.persistence.DelegatingPersistor
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor

data class PersistenceContext(private val persistenceExecutor: () -> Unit) {
    fun persist() = persistenceExecutor()
}

interface PersistenceAware {
    @get:JsonIgnore
    var persistenceContext: PersistenceContext
    fun statusUpdated() = persistenceContext.persist()
}

abstract class AbstractPersistenceAware : PersistenceAware {
    override lateinit var persistenceContext: PersistenceContext
    override fun statusUpdated() {
        if (this::persistenceContext.isInitialized)
            super.statusUpdated()
    }
}

interface PersistenceAwareWrapper<T : PersistenceAware> : PersistenceAware {
    @get:JsonIgnore
    val wrapped: T

    override var persistenceContext: PersistenceContext
        get() = wrapped.persistenceContext
        set(ctx) {
            wrapped.persistenceContext = ctx
        }
}


/**
 * A Persistor Wrapper which purpose is only to inject the persistence context to the PersistenceAware beans.
 */
class PersistenceAwarePersistor<T : PersistenceAware, C>(
    wrapped: SimplePersistor<T, C>,
) : DelegatingPersistor<T, T, C>(
    wrapped,
    BidirectionalMapper.identity(),
    wrapped.default,
) {

    init {
        this.observer =
            object :
                DelegatingPersistencyObserver<T, T>(
                    wrapped.observer,
                    BidirectionalMapper.identity()
                ) {
                override fun afterValueChanges(value: T) {
                    super.afterValueChanges(value)
                    value.persistenceContext = PersistenceContext { persist(value) }
                }
            }
    }

}


