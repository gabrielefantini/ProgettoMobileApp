package it.polito.mad.group25.lab.utils.persistence.awareds

import it.polito.mad.group25.lab.utils.persistence.impl.AbstractPersistenceAware

fun <T> persistenceAwareMutableListOf(vararg elements: T): PersistenceAwareMutableList<T> =
    PersistenceAwareMutableList<T>().apply { addAll(elements) }

class PersistenceAwareMutableList<T> : MutableList<T>,
    AbstractPersistenceAware() {

    private val innerList = mutableListOf<T>()
    override val size: Int
        get() = innerList.size

    override fun contains(element: T): Boolean = innerList.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = innerList.containsAll(elements)

    override fun get(index: Int): T = innerList[index]

    override fun indexOf(element: T): Int = innerList.indexOf(element)

    override fun isEmpty(): Boolean = innerList.isEmpty()

    override fun iterator(): MutableIterator<T> = innerList.iterator()

    override fun lastIndexOf(element: T): Int = innerList.lastIndexOf(element)

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

    override fun listIterator(): MutableListIterator<T> = innerList.listIterator()

    override fun listIterator(index: Int): MutableListIterator<T> = innerList.listIterator(index)

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

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> =
        innerList.subList(fromIndex, toIndex)

}