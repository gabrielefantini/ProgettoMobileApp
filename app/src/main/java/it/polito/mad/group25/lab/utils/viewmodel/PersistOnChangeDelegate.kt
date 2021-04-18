package it.polito.mad.group25.lab.utils.viewmodel

import android.content.Context
import android.content.SharedPreferences
import it.polito.mad.group25.lab.utils.persistence.DifferentiatedSerdeStrategy
import it.polito.mad.group25.lab.utils.tryInstantiate
import it.polito.mad.group25.lab.utils.type
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

interface PersistableContainer {
    fun getContext(): Context
}

class PersistOnChange<T> private constructor(
    private val hasDefault: Boolean,
    private val default: T?,
    private val customStrategy: DifferentiatedSerdeStrategy<T, SharedPreferences, SharedPreferences.Editor>? = null
) : ReadWriteProperty<PersistableContainer, T> {

    constructor(
        customStrategy: DifferentiatedSerdeStrategy<T, SharedPreferences, SharedPreferences.Editor>
    ) : this(false, null, customStrategy)

    constructor(
        default: T,
        customStrategy: DifferentiatedSerdeStrategy<T, SharedPreferences, SharedPreferences.Editor>? = null
    ) : this(true, default, customStrategy)

    private var loaded = false
    private var value: T? = null

    override fun setValue(thisRef: PersistableContainer, property: KProperty<*>, value: T) {
        persist(
            getStorage(thisRef),
            customStrategy,
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
            customStrategy,
            property.type().java as Class<T>,
            extractId(property)
        )
        loaded = true

        if (!hasDefault)
            return value as T

        return if (value == null)
            default as T
        else value as T
    }

    private fun getStorage(container: PersistableContainer) = container.getContext().let {
        it.getSharedPreferences(it::class.java.canonicalName, Context.MODE_PRIVATE)
    }

    private fun extractId(property: KProperty<*>): String =
        property.javaField!!.let { "${it.declaringClass.canonicalName}.${it.name}" }

    private fun persist(
        storage: SharedPreferences,
        customStrategy: DifferentiatedSerdeStrategy<T, SharedPreferences, SharedPreferences.Editor>?,
        id: String,
        value: T
    ) {
        val storageEditor = storage.edit()
        if (customStrategy != null)
            customStrategy(value, id, storageEditor)
        else storageEditor.putString(id, value.toString())
        storageEditor.apply()
    }

    private fun loadPersistence(
        storage: SharedPreferences,
        customStrategy: DifferentiatedSerdeStrategy<T, SharedPreferences, SharedPreferences.Editor>?,
        typo: Class<T>,
        id: String
    ): T? {
        return if (customStrategy != null)
            customStrategy(id, storage)
        else storage.getString(id, null)?.let { typo.tryInstantiate(it) }
    }
}


