package it.polito.mad.group25.lab.utils.persistence.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import it.polito.mad.group25.lab.utils.persistence.ConcurrentPersistor
import it.polito.mad.group25.lab.utils.persistence.PersistableContainer
import kotlin.reflect.KProperty


class SharedPreferencesPersistorDelegate<T>(
    default: T,
    private val typeReference: TypeReference<T>? = null,
    thisRef: PersistableContainer,
    property: KProperty<*>,
    onValueChanges: (T) -> Unit = {},
    onLoadedPersistedValue: (T) -> Unit = {},
) : ConcurrentPersistor<T, PersistableContainer>(
    thisRef,
    property,
    default,
    onValueChanges,
    onLoadedPersistedValue
) {

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val storage = thisRef.getStorage()

    override fun persist(value: T) {
        val storageEditor = storage.edit()
        storageEditor.putString(id, objectMapper.writeValueAsString(value))
        storageEditor.apply()
    }

    override fun loadPersistence(): T? {
        return if (typeReference != null)
            storage.getString(id, null)?.let { objectMapper.readValue(it, typeReference) }
        else storage.getString(id, null)?.let { objectMapper.readValue(it, targetClass) }
    }

}
