package it.polito.mad.group25.lab.utils.persistence.awareds

import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware

fun <T> persistenceAwareMutableSetOf(vararg elements: T): PersistenceAwareMutableSet<T> =
    PersistenceAwareMutableSet(mutableSetOf(*elements))


class PersistenceAwareMutableSet<T>(private val innerSet: MutableSet<T>) :
    AbstractPersistenceAware(), MutableSet<T> by innerSet {

    constructor() : this(mutableSetOf())

    override fun add(element: T): Boolean = performAndUpdateOnSuccess { innerSet.add(element) }

    override fun addAll(elements: Collection<T>): Boolean =
        performAndUpdateOnSuccess { innerSet.addAll(elements) }

    override fun clear() {
        innerSet.clear()
        statusUpdated()
    }

    override fun remove(element: T): Boolean =
        performAndUpdateOnSuccess { innerSet.remove(element) }

    override fun removeAll(elements: Collection<T>): Boolean =
        performAndUpdateOnSuccess { innerSet.removeAll(elements) }

    override fun retainAll(elements: Collection<T>): Boolean =
        performAndUpdateOnSuccess { innerSet.retainAll(elements) }

    private fun performAndUpdateOnSuccess(action: () -> Boolean): Boolean {
        val result = action()
        if (result)
            statusUpdated()
        return result
    }

}