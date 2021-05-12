package it.polito.mad.group25.lab.utils.persistence

import kotlin.properties.ReadWriteProperty

interface Persistor<T, C> : ReadWriteProperty<C, T> {
    fun persist()
    fun loadPersistence(): T?
}


