package it.polito.mad.group25.lab.utils.viewmodel

import android.content.Context
import android.content.SharedPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.mad.group25.lab.utils.type
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

interface PersistableContainer {
    fun getContext(): Context
}

class Persistor<T>(private val default: T) :
    ReadWriteProperty<PersistableContainer, T> {

    private companion object {
        val objectMapper = ObjectMapper()
    }

    private var loaded = false
    private var value: T? = null

    override fun setValue(thisRef: PersistableContainer, property: KProperty<*>, value: T) {
        persist(
            getStorage(thisRef),
            extractId(property),
            value
        )
        this.value = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: PersistableContainer, property: KProperty<*>): T {
        if (loaded) {
            return value as T
        }

        value = loadPersistence(
            getStorage(thisRef),
            property.type().java as Class<T>,
            extractId(property)
        )
        loaded = true

        if (value == null)
            value = default
        return value as T
    }

    private fun getStorage(container: PersistableContainer) = container.getContext().let {
        it.getSharedPreferences(it::class.java.canonicalName, Context.MODE_PRIVATE)
    }

    private fun extractId(property: KProperty<*>): String =
        property.javaField!!.let { "${it.declaringClass.canonicalName}.${it.name}" }

    private fun persist(
        storage: SharedPreferences,
        id: String,
        value: T
    ) {
        val storageEditor = storage.edit()
        storageEditor.putString(id, objectMapper.writeValueAsString(value))
        storageEditor.apply()
    }

    private fun loadPersistence(
        storage: SharedPreferences,
        typo: Class<T>,
        id: String
    ): T? {
        return storage.getString(id, null)?.let { objectMapper.readValue(it, typo) }
    }
}


