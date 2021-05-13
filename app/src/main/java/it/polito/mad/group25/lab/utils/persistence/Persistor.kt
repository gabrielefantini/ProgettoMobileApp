package it.polito.mad.group25.lab.utils.persistence

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

interface Persistor<T, C> : ReadWriteProperty<C, T> {
    fun persist()
    fun loadPersistence(): T?
}

fun <P : Persistor<*, *>> extractPersistor(field: KProperty0<*>) =
    field.apply { isAccessible = true }.getDelegate() as P

