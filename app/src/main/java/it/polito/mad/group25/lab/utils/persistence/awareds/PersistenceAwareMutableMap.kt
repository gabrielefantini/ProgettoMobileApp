package it.polito.mad.group25.lab.utils.persistence.awareds

import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware

fun <K, V> persistenceAwareMutableMapOf(vararg pairs: Pair<K, V>): PersistenceAwareMutableMap<K, V> =
    PersistenceAwareMutableMap(mutableMapOf(*pairs))

fun <K, V> persistenceAwareMutableLiveMapOf(vararg pairs: Pair<K, V>): PersistenceAwareMutableLiveMap<K, V> =
    PersistenceAwareMutableLiveMap(persistenceAwareMutableMapOf(*pairs))

class PersistenceAwareMutableMap<K, V>(private val innerMap: MutableMap<K, V>) :
    AbstractPersistenceAware(), MutableMap<K, V> by innerMap {

    constructor() : this(mutableMapOf())

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

class PersistenceAwareMutableLiveMap<K, V>(private val innerMap: PersistenceAwareMutableMap<K, V>) :
    AbstractPersistenceAwareLiveDataStructure<PersistenceAwareMutableLiveMap<K, V>>(innerMap),
    MutableMap<K, V> by innerMap {
    constructor() : this(persistenceAwareMutableMapOf())
}