package it.polito.mad.group25.lab.utils.persistence

import it.polito.mad.group25.lab.utils.BidirectionalMapper
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KProperty


interface PersistencyObserver<T> {

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
     * intercepts the attempt to performing persistency of a value and can override it by returning a new one.
     * If a null value is returned the persistency will not take place.
     */
    fun beforePerformingPersistency(value: T): T? = value

    /**
     * intercepts the instant after a persistency attempt was made.
     * If an exception was thrown during the persistency attempt it is received by the observer which can handle it or rethrow it.
     */
    fun afterPerformingPersistency(value: T, ex: Exception?) {
        if (ex != null) throw ex
    }

}

@Suppress("UNCHECKED_CAST")
abstract class SimplePersistor<T, C> : Persistor<T, C> {
    var observer: PersistencyObserver<T>
    protected val thisRef: C
    val default: T
    val id: String
    protected var value: T
        set(value) {
            field = observer.beforeValueChanges(field, value) ?: return
            observer.afterValueChanges(field)
        }


    // Does not work as primary constructor! Asks for value assignment
    constructor(
        thisRef: C,
        id: String,
        default: T,
        observer: PersistencyObserver<T> = object : PersistencyObserver<T> {}
    ) {
        this.default = default
        this.thisRef = thisRef
        this.observer = observer
        this.id = id
        value = loadPersistence() ?: default
    }

    constructor(other: SimplePersistor<T, C>) : this(
        other.thisRef,
        other.id,
        other.default,
        other.observer
    )

    constructor(
        other: SimplePersistor<*, C>,
        default: T,
        observer: PersistencyObserver<T> = object : PersistencyObserver<T> {}
    ) : this(
        other.thisRef,
        other.id,
        default,
        observer
    )


    override fun setValue(thisRef: C, property: KProperty<*>, value: T) {
        persist(value)
        this.value = value
    }

    override fun getValue(thisRef: C, property: KProperty<*>): T = value


    final override fun persist(value: T) {
        var ex: Exception? = null
        var toPersist: T? = null
        try {
            toPersist = observer.beforePerformingPersistency(value) ?: return
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
    id: String,
    default: T,
    observer: PersistencyObserver<T> = object : PersistencyObserver<T> {}
) : SimplePersistor<T, C>(thisRef, id, default, observer) {


    private val lock = ReentrantReadWriteLock()

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) =
        lock.write { super.setValue(thisRef, property, value) }

    override fun getValue(thisRef: C, property: KProperty<*>): T =
        lock.read { super.getValue(thisRef, property) }

}

/**
 * Persistency Observer which wraps another one and can customize its logic.
 */
open class DelegatingPersistencyObserver<A, B>(
    private val delegate: PersistencyObserver<B>,
    private val mapper: BidirectionalMapper<A, B>
) : PersistencyObserver<A> {

    override fun afterLoadingPersistedValue(value: A?, ex: Exception?): A? {
        return delegate.afterLoadingPersistedValue(value?.let { mapper.directMap(it) }, ex)
            ?.let { mapper.reverseMap(it) }
    }

    override fun afterPerformingPersistency(value: A, ex: Exception?) {
        mapper.directMap(value)?.let { delegate.afterPerformingPersistency(it, ex) }
    }

    override fun afterValueChanges(value: A) {
        mapper.directMap(value)?.let { delegate.afterValueChanges(it) }
    }

    override fun beforeLoadingPersistedValue(): Boolean {
        return delegate.beforeLoadingPersistedValue()
    }

    override fun beforePerformingPersistency(value: A): A? {
        return mapper.directMap(value)?.let {
            delegate.beforePerformingPersistency(it)?.let { it1 -> mapper.reverseMap(it1) }
        }
    }

    override fun beforeValueChanges(oldValue: A, newValue: A): A? {
        val oldMapped = mapper.directMap(oldValue)
        val newMapped = mapper.directMap(newValue)
        return if (oldMapped != null && newMapped != null)
            delegate.beforeValueChanges(oldMapped, newMapped)?.let { mapper.reverseMap(it) }
        else null
    }
}

/**
 * Wrapper which acts as a persistor of A but delegates, through mappers, to a persistor of B.
 * Created for all those classes which are only a wrapper of another class which is the one to be persisted, example LiveData.
 */
open class DelegatingPersistor<A, B, C>(
    protected val wrapped: SimplePersistor<B, C>,
    protected val mapper: BidirectionalMapper<A, B>,
    default: A,
    observer: DelegatingPersistencyObserver<A, B> =
        DelegatingPersistencyObserver(wrapped.observer, mapper)
) : SimplePersistor<A, C>(wrapped, default, observer) {

    override fun doLoadPersistence(): A? =
        wrapped.doLoadPersistence()?.let { mapper.reverseMap(it) }

    override fun doPersist(value: A) {
        mapper.directMap(value)?.let { wrapped.doPersist(it) }
    }

}


