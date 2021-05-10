package it.polito.mad.group25.lab.utils.persistence.awareds

import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware

fun <T> persistenceAwareMutableListOf(vararg elements: T): PersistenceAwareMutableList<T> =
    PersistenceAwareMutableList(mutableListOf(*elements))

class PersistenceAwareMutableList<T>(private val innerList: MutableList<T>) :
    MutableList<T> by innerList, AbstractPersistenceAware() {


    constructor() : this(mutableListOf())

    override fun add(element: T): Boolean {
        val result = innerList.add(element)
        if (result)
            statusUpdated()
        return result
    }

    override fun add(index: Int, element: T) {
        innerList.add(index, element)
        statusUpdated()
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val result = innerList.addAll(index, elements)
        if (result)
            statusUpdated()
        return result
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val result = innerList.addAll(elements)
        if (result)
            statusUpdated()
        return result
    }

    override fun clear() {
        innerList.clear()
        statusUpdated()
    }


    override fun remove(element: T): Boolean {
        val result = innerList.remove(element)
        if (result)
            statusUpdated()
        return result
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val result = removeAll(elements)
        if (result)
            statusUpdated()
        return result
    }

    override fun removeAt(index: Int): T {
        val result = removeAt(index)
        statusUpdated()
        return result
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val result = innerList.retainAll(elements)
        if (result)
            statusUpdated()
        return result
    }

    override fun set(index: Int, element: T): T {
        val toRet = innerList.set(index, element)
        statusUpdated()
        return toRet
    }

}