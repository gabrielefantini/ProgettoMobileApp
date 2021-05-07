package it.polito.mad.group25.lab.utils.persistence

import it.polito.mad.group25.lab.utils.type
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField


@Suppress("UNCHECKED_CAST")
abstract class SimplePersistor<T, C>(
    protected val thisRef: C,
    protected val property: KProperty<*>,
    protected val default: T,
    protected val onValueChanges: (T) -> Unit = {},
    protected val onLoadedPersistedValue: (T) -> Unit = {},
) : Persistor<T, C> {

    protected val id = property.javaField!!.let { "${it.declaringClass.canonicalName}.${it.name}" }
    protected val targetClass = property.type().java as Class<T>
    protected var value: T? = null


    init {
        initValue()
    }

    protected open fun initValue() {
        val loaded = loadPersistence()
        if (loaded == null) assignValue(default)
        else {
            onLoadedPersistedValue(loaded)
            assignValue(loaded)
        }
    }

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) {
        persist(value)
        assignValue(value)
    }

    private fun assignValue(value: T) {
        this.value = value
        onValueChanges(value)
    }

    override fun getValue(thisRef: C, property: KProperty<*>): T = value as T

}


abstract class ConcurrentPersistor<T, C>(
    thisRef: C,
    property: KProperty<*>,
    default: T,
    onValueChanges: (T) -> Unit = {},
    onLoadedPersistedValue: (T) -> Unit = {},
) : SimplePersistor<T, C>(thisRef, property, default, onValueChanges, onLoadedPersistedValue) {


    private val lock = ReentrantReadWriteLock()

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) =
        lock.write { super.setValue(thisRef, property, value) }

    override fun getValue(thisRef: C, property: KProperty<*>): T =
        lock.read { super.getValue(thisRef, property) }

}


