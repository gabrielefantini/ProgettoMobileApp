package it.polito.mad.group25.lab.utils.persistence.impl

import android.content.Context
import android.content.SharedPreferences
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.ConcurrentPersistor
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver


interface SharedPreferencesPersistableContainer {
    fun getContext(): Context

    fun getStorage(): SharedPreferences =
        getContext().getSharedPreferences(this::class.java.canonicalName, Context.MODE_PRIVATE)
}

class SharedPreferencesPersistorDelegate<T>(
    thisRef: SharedPreferencesPersistableContainer,
    id: String,
    targetClass: Class<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?
) : ConcurrentPersistor<T, SharedPreferencesPersistableContainer>(
    thisRef,
    id,
    targetClass,
    default,
    observer,
    handler
) {

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val storage = thisRef.getStorage()

    override fun <R> doPersist(value: R) {
        val storageEditor = storage.edit()
        storageEditor.putString(id, objectMapper.writeValueAsString(value))
        storageEditor.apply()
    }

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? = storage.getString(id, null)
        ?.let { objectMapper.readValue(it, object : TypeReference<R>() {}) }


}
