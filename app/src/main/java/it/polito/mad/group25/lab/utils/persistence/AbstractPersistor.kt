package it.polito.mad.group25.lab.utils.persistence

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KProperty


interface PersistenceObserver<T> {

    /**
     * intercepts the loading of the persisted value, has to return a boolean which is true if the loading can proceed
     */
    fun beforeLoadingPersistedValue(): Boolean = true

    /**
     * intercepts the loading of the persisted value and can override it by returning a new one.
     * If an exception was thrown during the loading attempt it is received by the observer which can handle it or rethrow it.
     * If a null value is returned the default provided to the persistor will be assigned.
     */
    fun afterLoadingPersistedValue(value: T?, ex: Exception?): T? {
        if (ex != null)
            throw ex
        return value
    }

    /**
     * intercepts the changing attempt. Can override the new value by changing the return value.
     * If a null value is returned the assignment will not take place.
     */
    fun beforeValueChanges(oldValue: T, newValue: T): T? = newValue

    /**
     * intercepts the instant after the value has been changed.
     */
    fun afterValueChanges(value: T) {}

    /**
     * intercepts the attempt to performing persistence of a value and can override it by returning a new one.
     * If a null value is returned the persistence will not take place.
     */
    fun beforePerformingPersistence(value: T): T? = value

    /**
     * intercepts the instant after a persistence attempt was made.
     * If an exception was thrown during the persistence attempt it is received by the observer which can handle it or rethrow it.
     */
    fun afterPerformingPersistence(value: T, ex: Exception?) {
        if (ex != null) throw ex
    }

}


/**
 * Generic class  which purpose is to hold the strategies to handle persistence
 */
abstract class PersistenceHandler<T> {
    /**
     * Handle object changing. Return null to deny changing.
     * This is called <b>before</b> the delegate observer.
     */
    abstract fun handleNewValue(oldValue: T, newValue: T): T?

    /**
     * Handle object changing in case old value its not available. Return null to deny changing.
     * This is called <b>before</b> the delegate observer.
     */
    abstract fun handleNewValue(newValue: T): T?
    abstract fun handlePersistenceRequest(value: T)
    abstract fun handlePersistenceLoading(clazz: Class<T>): T?

    /**
     * Used to notify the handlers that the current value is being loaded so it has not be serialized
     * even if it is changed. (ex insertion on PersistenceAware Collections and so on)
     */
    private val isCurrentlyLoadingPersistence = AtomicBoolean(false)
    protected val currentlyLoadingPersisenceFlag = ReentrantReadWriteLock()
    open fun notifyPersistenceLoading() =
        currentlyLoadingPersisenceFlag.write { isCurrentlyLoadingPersistence.set(true) }

    open fun notifyPersistenceLoadingCompleted() =
        currentlyLoadingPersisenceFlag.write { isCurrentlyLoadingPersistence.set(false) }

    protected fun isCurrentlyLoadingPersistence(): Boolean =
        currentlyLoadingPersisenceFlag.read { isCurrentlyLoadingPersistence.get() }
}


/**
 * Abstract handler which leverages on another handler.
 * Created to address those situations in which more handlers are applicable
 * The deepest inner handler will be initialized by the persistor delegate.
 * WARNING: THE NEW VALUE HANDLING HAS TO BE CALLED IN A DEPTH-FIRST TRAVERSAL, SO THE HANDLER HAS
 * TO WORK ON THE VALUE RETURNED BY ITS INNER HANDLER.
 */
abstract class AbstractPersistenceHandler<T, I>(nextHandler: PersistenceHandler<I>? = null) :
    PersistenceHandler<T>() {
    lateinit var innerHandler: PersistenceHandler<I>


    init {
        if (nextHandler != null)
            innerHandler = nextHandler
    }

    fun isInitialized() = this::innerHandler.isInitialized

    final override fun notifyPersistenceLoading() =
        currentlyLoadingPersisenceFlag.write {
            super.notifyPersistenceLoading()
            if (isInitialized())
                innerHandler.notifyPersistenceLoading()
        }

    final override fun notifyPersistenceLoadingCompleted() =
        currentlyLoadingPersisenceFlag.write {
            super.notifyPersistenceLoadingCompleted()
            if (isInitialized())
                innerHandler.notifyPersistenceLoadingCompleted()
        }
}


@Suppress("UNCHECKED_CAST")
abstract class SimplePersistor<T, C> : Persistor<T, C> {
    companion object {
        val LOG_TAG = "PERSISTENCE DELEGATES"
    }

    private var defaultLoaded: Boolean = false
    val isReadOnly = AtomicBoolean(false)
    private var isInitialized = false
    var observer: PersistenceObserver<T>
    protected val thisRef: C
    val default: T
    val id: String
    protected val targetClass: Class<T>
    protected val handler: PersistenceHandler<T>
    protected var value: T
        set(value) {
            if (!defaultLoaded) {
                field = value
                return
            }
            assertNotReadOnly()

            Log.d(LOG_TAG, "Calling handler for changing value attempt of $id")

            var toSet: T? = if (field != null)
                handler.handleNewValue(field, value)
            else handler.handleNewValue(value)

            if (toSet == null) {
                Log.w(LOG_TAG, "Handler of $id denied value changing.")
                return
            }
            Log.d(LOG_TAG, "Providing value change request of $id to observer")
            toSet = observer.beforeValueChanges(field, toSet)
            if (toSet == null) {
                Log.w(LOG_TAG, "Observer denied value changing of $id.")
                return
            } else {
                Log.d(LOG_TAG, "Setting new value $toSet to $id")
                field = toSet
                Log.d(LOG_TAG, "Calling observer with new changed value of $id")
                observer.afterValueChanges(field)
            }
        }


    // Does not work as primary constructor! Asks for value assignment
    constructor(
        thisRef: C,
        id: String,
        targetClass: Class<T>,
        default: T,
        observer: PersistenceObserver<T>,
        handler: AbstractPersistenceHandler<T, *>?
    ) {
        this.default = default
        this.thisRef = thisRef
        this.observer = observer
        this.id = id
        this.targetClass = targetClass
        this.handler = initializeHandler(handler)
        this.handler.notifyPersistenceLoading()
        this.value = default
        this.defaultLoaded = true
        this.handler.notifyPersistenceLoadingCompleted()
    }

    /**
     * Used to start the persistence loading after the child has initialized all it's value
     */
    protected open fun initialized() {
        isInitialized = true
        notifyingPersistenceLoading { this.value = loadPersistence() ?: default }
    }

    private fun initializeHandler(handler: AbstractPersistenceHandler<T, *>?): PersistenceHandler<T> =
        handler?.apply(this::doInitializeHandlerRecursively) ?: basicHandler()

    private fun <A, B> doInitializeHandlerRecursively(handler: AbstractPersistenceHandler<A, B>) {
        if (!handler.isInitialized())
            handler.apply { innerHandler = basicHandler() }
        else doInitializeHandlerRecursively(handler.innerHandler as AbstractPersistenceHandler<*, *>)
    }

    private fun <R> basicHandler(): PersistenceHandler<R> =
        object : AbstractPersistenceHandler<R, R>() {
            override fun handlePersistenceLoading(clazz: Class<R>): R? = doLoadPersistence(clazz)

            override fun handlePersistenceRequest(value: R) {
                assertNotReadOnly()
                if (!isCurrentlyLoadingPersistence()) doPersist(value)
            }

            override fun handleNewValue(oldValue: R, newValue: R): R? = newValue

            override fun handleNewValue(newValue: R): R? = newValue
        }


    override fun setValue(thisRef: C, property: KProperty<*>, value: T) {
        Log.d(LOG_TAG, "${this.javaClass.simpleName} intercepted a value change of $id.")
        persist(value)
        this.value = value
    }

    override fun getValue(thisRef: C, property: KProperty<*>): T = value


    final override fun persist(value: T) {
        assertNotReadOnly()
        Log.i(
            LOG_TAG,
            "Performing persistence of new value of $id through ${this.javaClass.simpleName}"
        )
        var ex: Exception? = null
        var toPersist: T? = null
        try {
            Log.d(LOG_TAG, "Calling observer for persistence attempt of $id.")
            toPersist = observer.beforePerformingPersistence(value)
            if (toPersist == null) {
                Log.w(LOG_TAG, "Observer denied persistence of $id with value $value.")
                return
            }
            handler.handlePersistenceRequest(value)
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Exception while performing persistence of $id!", e)
            ex = e
        }
        Log.d(LOG_TAG, "Calling observer after persistence attempt of $id.")
        observer.afterPerformingPersistence(toPersist!!, ex)
    }

    final override fun loadPersistence(): T? {
        Log.i(LOG_TAG, "Loading persisted value for $id through ${javaClass.simpleName}")
        var ex: Exception? = null
        var loaded: T? = null
        try {
            Log.d(LOG_TAG, "Calling observer before loading persisted value for $id.")
            if (!observer.beforeLoadingPersistedValue()) {
                Log.w(LOG_TAG, "Observer denied loading persisted value of $id")
                return null
            }
            loaded = handler.handlePersistenceLoading(targetClass)
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Exception while loading persisted value of $id!")
            ex = e
        }
        Log.d(LOG_TAG, "Calling observer after loaded persisted value of $id")
        return observer.afterLoadingPersistedValue(loaded, ex)
    }

    protected fun loadPersistenceAndSaveIt() =
        notifyingPersistenceLoading { loadPersistence()?.let { this.value = it } }


    protected fun <P> notifyingPersistenceLoading(action: () -> P): P {
        try {
            this.handler.notifyPersistenceLoading()
            return action()
        } finally {
            this.handler.notifyPersistenceLoadingCompleted()
        }
    }

    protected fun assertNotReadOnly() {
        if (isReadOnly.get())
            throw IllegalAccessException("Persistor ${this::class.java.simpleName} of $id is readOnly!")
    }

    //other generic types because the handler can customize the value to be serialized or loaded.
    //the persistor has to be able to load and persist every value.
    //per value type persistence is up to the handlers.
    abstract fun <R> doLoadPersistence(targetClass: Class<R>): R?
    abstract fun <R> doPersist(value: R)

}


abstract class ConcurrentPersistor<T, C>(
    thisRef: C,
    id: String,
    targetClass: Class<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?
) : SimplePersistor<T, C>(thisRef, id, targetClass, default, observer, handler) {


    private val lock = ReentrantReadWriteLock()

    override fun initialized() =
        lock.write { super.initialized() }

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) =
        lock.write { super.setValue(thisRef, property, value) }

    override fun getValue(thisRef: C, property: KProperty<*>): T =
        lock.read { super.getValue(thisRef, property) }

}



