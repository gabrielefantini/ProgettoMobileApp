package it.polito.mad.group25.lab.utils.persistence

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

data class PersistenceContext(private val persistenceExecutor: (PersistenceAware) -> Unit) {
    fun persist(instance: PersistenceAware) = persistenceExecutor(instance)
}

interface PersistenceAware {
    @get:JsonIgnore
    var persistenceContext: PersistenceContext
    fun statusUpdated() = persistenceContext.persist(this)
    fun isInitialized(): Boolean

    // use to delegate on object fields which changes implies status update.
    fun <T, C> onChangeUpdateStatus(default: T) =
        object : ReadWriteProperty<C, T> {
            private var value: T = default

            override fun setValue(thisRef: C, property: KProperty<*>, value: T) {
                if (this.value is PersistenceAware && value is PersistenceAware && !value.isInitialized()) {
                    value.persistenceContext = (this.value as PersistenceAware).persistenceContext
                }
                this.value = value
                statusUpdated()
            }

            override fun getValue(thisRef: C, property: KProperty<*>): T = value
        }

}

abstract class AbstractPersistenceAware : PersistenceAware {
    override lateinit var persistenceContext: PersistenceContext
    override fun statusUpdated() {
        if (this.isInitialized())
            super.statusUpdated()
    }

    override fun isInitialized() = this::persistenceContext.isInitialized
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


class PersistenceAwarePersistenceHandler<T : PersistenceAware>(nextHandler: PersistenceHandler<T>? = null) :
    AbstractPersistenceHandler<T, T>(nextHandler) {

    override fun handleNewValue(oldValue: T, newValue: T): T? =
        innerHandler.handleNewValue(oldValue, newValue)?.let(this::injectPersistenceContext)

    override fun handleNewValue(newValue: T): T? =
        innerHandler.handleNewValue(newValue)?.let(this::injectPersistenceContext)

    override fun handlePersistenceRequest(value: T) = innerHandler.handlePersistenceRequest(value)

    override fun handlePersistenceLoading(clazz: Class<T>): T? =
        innerHandler.handlePersistenceLoading(clazz)


    private fun injectPersistenceContext(value: T) =
        value.apply {
            this.persistenceContext =
                PersistenceContext { handlePersistenceRequest(it as T) }
        }
}