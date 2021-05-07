package it.polito.mad.group25.lab.utils.persistence.impl

import android.content.Context
import android.content.SharedPreferences
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import it.polito.mad.group25.lab.utils.persistence.ConcurrentPersistor
import it.polito.mad.group25.lab.utils.persistence.PersistencyObserver
import kotlin.reflect.KProperty


interface SharedPreferencesPersistableContainer {
    fun getContext(): Context

    fun getStorage(): SharedPreferences =
        getContext().getSharedPreferences(this::class.java.canonicalName, Context.MODE_PRIVATE)
}

class SharedPreferencesPersistorDelegate<T>(
    default: T,
    private val typeReference: TypeReference<T>? = null,
    thisRef: SharedPreferencesPersistableContainer,
    property: KProperty<*>,
    observer: PersistencyObserver<T> = object : PersistencyObserver<T>() {}
) : ConcurrentPersistor<T, SharedPreferencesPersistableContainer>(
    thisRef,
    property,
    default,
    observer
) {

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val storage = thisRef.getStorage()

    override fun doPersist(value: T) {
        val storageEditor = storage.edit()
        storageEditor.putString(id, objectMapper.writeValueAsString(value))
        storageEditor.apply()
    }

    override fun doLoadPersistence(): T? {
        return if (typeReference != null)
            storage.getString(id, null)?.let { objectMapper.readValue(it, typeReference) }
        else storage.getString(id, null)?.let { objectMapper.readValue(it, targetClass) }
    }

}
