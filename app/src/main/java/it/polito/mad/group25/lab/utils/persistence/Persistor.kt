package it.polito.mad.group25.lab.utils.persistence

import android.content.Context
import android.content.SharedPreferences
import com.fasterxml.jackson.annotation.JsonIgnore
import kotlin.properties.ReadWriteProperty

interface Persistor<T, C> : ReadWriteProperty<C, T> {
    fun persist(value: T)
    fun loadPersistence(): T?
}

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

interface PersistableContainer {
    fun getContext(): Context

    fun getStorage(): SharedPreferences =
        getContext().getSharedPreferences(this::class.java.canonicalName, Context.MODE_PRIVATE)
}

