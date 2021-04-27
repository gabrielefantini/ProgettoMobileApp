package it.polito.mad.group25.lab.utils.persistence.impl

import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware

fun <K, V> persistenceAwareMutableMapOf(vararg pairs: Pair<K, V>): PersistenceAwareMutableMap<K, V> =
    PersistenceAwareMutableMap<K, V>().apply { putAll(pairs.toMap()) }

class PersistenceAwareMutableMap<K, V> : AbstractPersistenceAware(), MutableMap<K, V> {
    private val innerMap = mutableMapOf<K, V>()
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = innerMap.entries
    override val keys: MutableSet<K>
        get() = innerMap.keys
    override val values: MutableCollection<V>
        get() = innerMap.values
    override val size: Int
        get() = innerMap.size

    override fun containsKey(key: K): Boolean = innerMap.containsKey(key)

    override fun containsValue(value: V): Boolean = innerMap.containsValue(value)

    override fun get(key: K): V? = innerMap[key]

    override fun isEmpty(): Boolean = innerMap.isEmpty()

    override fun clear() {
        innerMap.clear()
        statusUpdated()
    }

    override fun put(key: K, value: V): V? {
        val result = innerMap.put(key, value)
        statusUpdated()
        return result
    }

    override fun putAll(from: Map<out K, V>) {
        innerMap.putAll(from)
        statusUpdated()
    }

    override fun remove(key: K): V? {
        val result = innerMap.remove(key)
        statusUpdated()
        return result
    }
}