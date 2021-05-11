package it.polito.mad.group25.lab.utils.cache

import android.util.Log
import org.apache.commons.jcs.access.CacheAccess
import org.apache.commons.jcs.engine.CompositeCacheAttributes
import org.apache.commons.jcs.engine.ElementAttributes
import org.apache.commons.jcs.engine.control.CompositeCache

import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

data class CacheElement<V>(val value: V?, val elementAttributes: ElementAttributes)

abstract class ConcurrentCache<K, V>(cacheAttributes: CompositeCacheAttributes) {
    protected val innerCache: CacheAccess<K, V> =
        CacheAccess(CompositeCache(cacheAttributes, ElementAttributes()))
    private val readWriteLock: ReadWriteLock = ReentrantReadWriteLock()
    private val cacheName: String = cacheAttributes.cacheName

    private val logTag = "${javaClass.simpleName}.$cacheName"
    operator fun get(key: K): V? {
        Log.i(logTag, "Retrieving from $cacheName cache element with key $key")
        val lock = readWriteLock.readLock()
        lock.lock()
        Log.d("{} cache read lock acquired", cacheName)
        var value: V? = try {
            innerCache.get(key)
        } finally {
            lock.unlock()
            Log.d(logTag, "$cacheName cache read lock released")
        }
        if (value == null) {
            Log.i(logTag, "$cacheName cache miss for key $key")
            val autoConsistentValue = cacheMiss(key)
            autoConsistentValue?.let { value = updateCache(key) { it } }
        } else {
            Log.i(logTag, "$cacheName cache hit for key $key")
        }
        return value
    }

    private fun updateCache(key: K, valueProvider: () -> CacheElement<V>): V? {
        Log.i(logTag, "Updating cache $cacheName for element with key $key")
        val lock = readWriteLock.writeLock()
        lock.lock()
        Log.d(logTag, "$cacheName cache write lock acquired")
        val cacheMiss: CacheElement<V>
        try {
            cacheMiss = valueProvider()
            innerCache.put(key, cacheMiss.value, cacheMiss.elementAttributes)
            Log.i(logTag, "Value inserted in $cacheName cache for key $key")
        } finally {
            lock.unlock()
            Log.d(logTag, "$cacheName cache write lock released")
        }
        return cacheMiss.value
    }

    protected operator fun set(key: K, value: CacheElement<V>) {
        updateCache(key, { value })
    }


    protected open fun cacheMiss(key: K): CacheElement<V>? {
        return null
    }

}
