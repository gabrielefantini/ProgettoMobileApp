package it.polito.mad.group25.lab.utils.persistence.impl

import com.fasterxml.jackson.annotation.JsonIgnore
import it.polito.mad.group25.lab.utils.persistence.PersistencyObserver
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor
import kotlin.reflect.KProperty

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
class PersistenceAwareContextInjector<T : PersistenceAware, C>(
    thisRef: C,
    property: KProperty<*>,
    private val wrapped: SimplePersistor<T, C>,
    default: T,
    observer: PersistencyObserver<T> = object : PersistencyObserver<T>() {}
) : SimplePersistor<T, C>(thisRef, property, default, observer) {

    init {
        val persistorRef = this
        this.observer = object : PersistencyObserver<T>() { //TODO INGLOBA L'ALTRO
            override fun afterValueChanges(value: T) {
                super.afterValueChanges(value)
                value.persistenceContext = PersistenceContext { persistorRef.persist(value) }
            }
        }
    }

    override fun doLoadPersistence(): T? = wrapped.doLoadPersistence()

    override fun doPersist(value: T) = wrapped.doPersist(value)

}


