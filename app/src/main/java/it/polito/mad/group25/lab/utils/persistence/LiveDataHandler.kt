package it.polito.mad.group25.lab.utils.persistence

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

abstract class LiveDataPersistenceObserver<T>(inner: PersistenceObserver<LiveData<T>>) :
    PersistenceObserver<LiveData<T>> by inner {

    /**
     * Intercepts the live value changes.
     */
    open fun onLiveValueChanges(newValue: T) {}

    /**
     * Intercepts the attempt to persist a live value changing.
     * Can customize the persisted value by returning a new one
     */
    open fun beforePerformingLiveValuePersistency(value: T): T? = value

    /**
     * Intercepts the instant after a persistency attempt was made on the new live value.
     * If an exception was thrown during the persistency attempt it is received by the observer which can handle it or rethrow it.
     */
    open fun afterPerformingLiveValuePersistency(value: T, ex: Exception?) {}
}

class LiveDataPersistenceHandler<T, L : LiveData<T>>(
    private val isAutoUpdatable: Boolean,
    private val clazz: Class<L>,
    private val innerType: Class<T>,
    private val observer: LiveDataPersistenceObserver<T> = object :
        LiveDataPersistenceObserver<T>(object : PersistenceObserver<LiveData<T>> {}) {},
    nextHandler: PersistenceHandler<T>? = null,
) : AbstractPersistenceHandler<L, T>(nextHandler) {

    private fun doPersistInnerType(value: T) {
        var ex: Exception? = null
        var toPersist: T? = null
        try {
            toPersist = observer.beforePerformingLiveValuePersistency(value)

            if (toPersist == null) return

            innerHandler.handlePersistenceRequest(toPersist)
        } catch (e: Exception) {
            ex = e
        }
        if (toPersist != null) {
            observer.afterPerformingLiveValuePersistency(toPersist, ex)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun wrapInLiveData(innerValue: T): L {
        if (LiveData::class.java == clazz || MutableLiveData::class.java == clazz) {
            return MutableLiveData(innerValue) as L
        }
        if (MediatorLiveData::class.java == clazz)
            return MediatorLiveData<T>().apply { value = innerValue } as L

        throw NotImplementedError("Unknown type of LiveData ${clazz.canonicalName}")
    }

    override fun handleNewValue(oldValue: L, newValue: L): L =
        newValue.apply {
            if (this !== oldValue) //ensure value changed in order to avoid multiple subscriptions
                subscribeToLiveData(this)
        }

    override fun handleNewValue(newValue: L): L = newValue.let(this::subscribeToLiveData)

    override fun handlePersistenceRequest(value: L) {
        value.value?.let { it1 -> doPersistInnerType(it1) }
    }

    override fun handlePersistenceLoading(clazz: Class<L>): L? {
        return innerHandler.handlePersistenceLoading(innerType)?.let(this::wrapInLiveData)
    }

    private fun subscribeToLiveData(obj: L) = obj.apply {
        if (isAutoUpdatable)
            this.observeForever {
                val toPersist = innerHandler.handleNewValue(it) ?: return@observeForever
                observer.onLiveValueChanges(toPersist)
                doPersistInnerType(toPersist)
            }
    }

}
