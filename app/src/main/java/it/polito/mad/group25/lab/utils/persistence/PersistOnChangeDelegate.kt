package it.polito.mad.group25.lab.utils.persistence

import android.content.Context
import android.content.SharedPreferences
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import it.polito.mad.group25.lab.utils.persistence.PersistenceUtils.extractId
import it.polito.mad.group25.lab.utils.persistence.PersistenceUtils.getStorage
import it.polito.mad.group25.lab.utils.persistence.PersistenceUtils.loadPersistence
import it.polito.mad.group25.lab.utils.persistence.PersistenceUtils.persist
import it.polito.mad.group25.lab.utils.type
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

interface PersistableContainer {
    fun getContext(): Context
}

data class PersistenceContext(private val persistenceExecutor: () -> Unit) {
    fun persist() = persistenceExecutor()
}

interface PersistenceAware {
    @get:JsonIgnore
    var persistenceContext: PersistenceContext
    fun statusUpdated() = persistenceContext.persist()
}

abstract class AbstractPersistenceAware : PersistenceAware {
    override lateinit var persistenceContext: PersistenceContext
    override fun statusUpdated() {
        if (this::persistenceContext.isInitialized)
            super.statusUpdated()
    }
}

interface PersistenceAwareWrapper<T : PersistenceAware> : PersistenceAware {
    @get:JsonIgnore
    val wrapped: T

    override var persistenceContext: PersistenceContext
        get() = wrapped.persistenceContext
        set(ctx) {
            wrapped.persistenceContext = ctx
        }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class ConcurrentPersistor<T>(
    private val default: T,
    private val typeReference: TypeReference<T>? = null,
    private val onLoadedPersistedValue: (T?) -> Unit = {}
) :
    ReadWriteProperty<PersistableContainer, T> {


    private var loaded: AtomicBoolean = AtomicBoolean(false)
    private var value: T? = null
    private val lock = ReentrantReadWriteLock()

    override fun setValue(thisRef: PersistableContainer, property: KProperty<*>, value: T) {
        val writeLock = lock.writeLock()
        writeLock.lock()
        try {
            persist(
                getStorage(thisRef),
                extractId(property),
                value
            )
            handlePersistentAware(thisRef, property, value!!)
            this.value = value
            this.loaded.set(true)
        } finally {
            writeLock.unlock()
        }
    }


    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: PersistableContainer, property: KProperty<*>): T {
        val readLock = lock.readLock()
        readLock.lock()
        try {
            if (loaded.get()) {
                return value as T
            }
        } finally {
            readLock.unlock()
        }

        val writeLock = lock.writeLock()
        writeLock.lock()

        try {
            if (loaded.get()) {
                return value as T
            }

            value = if (typeReference != null)
                loadPersistence(
                    getStorage(thisRef),
                    typeReference,
                    extractId(property)
                )
            else
                loadPersistence(
                    getStorage(thisRef),
                    property.type().java as Class<T>,
                    extractId(property)
                )


            loaded.set(true)

            if (value == null)
                value = default
            else onLoadedPersistedValue(value)

            value?.let { handlePersistentAware(thisRef, property, it) }

            return value as T
        } finally {
            writeLock.unlock()
        }
    }

    private fun handlePersistentAware(
        thisRef: PersistableContainer,
        property: KProperty<*>,
        value: T
    ) {
        if (value is PersistenceAware)
            (value as PersistenceAware).persistenceContext = PersistenceContext {
                val writeLock = lock.writeLock()
                writeLock.lock()
                try {
                    persist(
                        getStorage(thisRef),
                        extractId(property),
                        value
                    )
                } finally {
                    writeLock.unlock()
                }
            }

    }
}

private object PersistenceUtils {
    val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    fun getStorage(container: PersistableContainer): SharedPreferences =
        container.getContext().let {
            it.getSharedPreferences(it::class.java.canonicalName, Context.MODE_PRIVATE)
        }

    fun extractId(property: KProperty<*>): String =
        property.javaField!!.let { "${it.declaringClass.canonicalName}.${it.name}" }

    fun <T> persist(
        storage: SharedPreferences,
        id: String,
        value: T
    ) {
        val storageEditor = storage.edit()
        storageEditor.putString(id, objectMapper.writeValueAsString(value))
        storageEditor.apply()
    }

    fun <T> loadPersistence(
        storage: SharedPreferences,
        typo: Class<T>,
        id: String
    ): T? {
        return storage.getString(id, null)?.let { objectMapper.readValue(it, typo) }
    }

    fun <T> loadPersistence(
        storage: SharedPreferences,
        typeReference: TypeReference<T>,
        id: String
    ): T? {
        return storage.getString(id, null)?.let { objectMapper.readValue(it, typeReference) }
    }
}


