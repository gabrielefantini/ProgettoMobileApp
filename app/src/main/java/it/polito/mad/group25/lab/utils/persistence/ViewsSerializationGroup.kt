package it.polito.mad.group25.lab.utils.persistence

import android.view.View

@FunctionalInterface
interface ViewLoader<T : View, Q> {
    fun load(id: String, view: T, storage: Q): Boolean
}

@FunctionalInterface
interface ViewSaver<T : View, Q> {
    fun save(id: String, view: T, target: Q)
}

interface ViewPersistor<T : View, Q> : ViewSaver<T, Q>, ViewLoader<T, Q>


class ViewsSerializationGroup<T : View, P> {
    val persistor: ViewPersistor<T, P>
    val idsMapped: List<Pair<Int, String>>

    constructor(persistor: ViewPersistor<T, P>, vararg ids: Pair<Int, String>) {
        this.persistor = persistor
        this.idsMapped = ids.toList()
    }

    constructor(persistor: ViewPersistor<T, P>, vararg simpleIds: Int) {
        this.persistor = persistor
        this.idsMapped = simpleIds.map { Pair(it, it.toString()) }.toList()
    }
}