package it.polito.mad.group25.lab.utils.persistence.impl

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.ConcurrentPersistor
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.toJavaType


interface SharedPreferencesPersistableContainer {
    fun getContext(): Context

    fun getStorage(): SharedPreferences =
        getContext().getSharedPreferences(this::class.java.canonicalName, Context.MODE_PRIVATE)
}

class SharedPreferencesPersistorDelegate<T>(
    thisRef: SharedPreferencesPersistableContainer,
    id: String,
    private val typeReference: TypeReference<T>,
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

    companion object {
        private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    init {
        initialized()
    }

    override fun <R> doPersist(value: R) {
        val storageEditor = thisRef.getStorage().edit()
        storageEditor.putString(id, objectMapper.writeValueAsString(value))
        storageEditor.apply()
    }

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? {
        val computedTypeReference = try {
            findSubJavaType(
                typeReference.toJavaType(),
                targetClass
            )
        } catch (_: Throwable) {
            Log.e(
                LOG_TAG,
                "Couldn't compute type reference for $id from ${typeReference.type.typeName}." +
                        " Will use raw target type ${targetClass.canonicalName} "
            )
            null
        }

        return thisRef.getStorage().getString(id, null)
            ?.let { v ->
                if (computedTypeReference != null)
                    objectMapper.readValue(v, computedTypeReference) as R
                else objectMapper.readValue(v, targetClass)
            }
    }


    /**
     * Find the generic sub type reference starting from the first one provided.
     */
    // La funzione doLoadPersistence deve essere indipendente dal tipo, ma a jackson non basta il
    // target type a causa della type erasure!! Esploro quindi il type reference iniziale alla ricerca
    // di un sottotipo (perchè di solito al doLoadPersistence arrivano sottotipi del tipo generico,
    // esempio: se ho LiveData<List<String>> mi arriva List<String>) alla ricerca del JavaType
    // corrispondete a quello della classe di cui bisogna fare il caricamento.
    private fun <R> findSubJavaType(levelJavaType: JavaType, targetClass: Class<R>): JavaType? {
        if (levelJavaType.rawClass == targetClass) return levelJavaType
        for (type in levelJavaType.bindings.typeParameters) {
            if (type.rawClass == targetClass) return type
            else {
                val nextType = findSubJavaType(type, targetClass)
                if (nextType != null)
                    return nextType
            }
        }
        return null
    }


}
