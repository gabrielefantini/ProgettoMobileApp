package it.polito.mad.group25.lab.utils.persistence

import it.polito.mad.group25.lab.utils.type
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField


abstract class PersistencyObserver<T> {

    /**
     * intercepts the loading of the persisted value, has to return a boolean which is true if the loading can proceed
     */
    open fun beforeLoadingPersistedValue(): Boolean = true

    /**
     * intercepts the loading of the persisted value and can override it by returning a new one.
     * If an exception was thrown during the loading attempt it is received by the observer which can handle it or rethrow it.
     */
    open fun afterLoadingPersistedValue(value: T?, ex: Exception?): T? {
        if (ex != null)
            throw ex
        return value
    }

    /**
     * intercepts the changing attempt. Can override the new value by changing the return value.
     * Can also deny the value changing by throwing an exception
     */
    open fun beforeValueChanges(oldValue: T, newValue: T): T = newValue

    /**
     * intercepts the moment after the value has been changed.
     */
    open fun afterValueChanges(value: T) {}

    /**
     * intercepts the attempt to performing persistency of a value and can override it by returning a new one.
     */
    open fun beforePerformingPersistency(value: T): T = value

    /**
     * intercepts the moment after a persistency attempt was made.
     * If an exception was thrown during the persistency attempt it is received by the observer which can handle it or rethrow it.
     */
    open fun afterPerformingPersistency(value: T, ex: Exception?) {
        if (ex != null) throw ex
    }

}

@Suppress("UNCHECKED_CAST")
abstract class SimplePersistor<T, C> : Persistor<T, C> {
    protected val thisRef: C
    protected val property: KProperty<*>
    protected val default: T
    protected var observer: PersistencyObserver<T>
    protected val id: String
    protected val targetClass: Class<T>
    protected var value: T
        set(value) {
            field = observer.beforeValueChanges(field, value)
            observer.afterValueChanges(field)
        }


    // Does not work as primary constructor! Asks for value assignment
    constructor(
        thisRef: C,
        property: KProperty<*>,
        default: T,
        observer: PersistencyObserver<T> = object : PersistencyObserver<T>() {}
    ) {
        this.default = default
        this.property = property
        this.thisRef = thisRef
        this.observer = observer
        this.id = property.javaField!!.let { "${it.declaringClass.canonicalName}.${it.name}" }
        this.targetClass = property.type().java as Class<T>
        value = loadPersistence() ?: default
    }

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) {
        persist(value)
        this.value = value
    }

    override fun getValue(thisRef: C, property: KProperty<*>): T = value


    final override fun persist(value: T) {
        var ex: Exception? = null
        var toPersist: T? = null
        try {
            toPersist = observer.beforePerformingPersistency(value)
            doPersist(toPersist)
        } catch (e: Exception) {
            ex = e
        }
        observer.afterPerformingPersistency(toPersist!!, ex)
    }

    final override fun loadPersistence(): T? {
        var ex: Exception? = null
        var loaded: T? = null
        try {
            if (!observer.beforeLoadingPersistedValue())
                return null
            loaded = doLoadPersistence()
        } catch (e: Exception) {
            ex = e
        }
        return observer.afterLoadingPersistedValue(loaded, ex)
    }

    abstract fun doLoadPersistence(): T?
    abstract fun doPersist(value: T)

}


abstract class ConcurrentPersistor<T, C>(
    thisRef: C,
    property: KProperty<*>,
    default: T,
    observer: PersistencyObserver<T> = object : PersistencyObserver<T>() {}
) : SimplePersistor<T, C>(thisRef, property, default, observer) {


    private val lock = ReentrantReadWriteLock()

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) =
        lock.write { super.setValue(thisRef, property, value) }

    override fun getValue(thisRef: C, property: KProperty<*>): T =
        lock.read { super.getValue(thisRef, property) }

}


