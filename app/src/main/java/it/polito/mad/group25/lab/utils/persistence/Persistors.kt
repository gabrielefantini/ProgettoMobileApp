package it.polito.mad.group25.lab.utils.persistence

import android.util.Log
import androidx.lifecycle.LiveData
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.extractClass
import it.polito.mad.group25.lab.utils.persistence.impl.SharedPreferencesPersistableContainer
import it.polito.mad.group25.lab.utils.persistence.impl.SharedPreferencesPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLiveCollectionPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLiveMapPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistorDelegate
import it.polito.mad.group25.lab.utils.toJavaType
import it.polito.mad.group25.lab.utils.type
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

//creato per separare la logica di scelta del persistor dal persistor stesso
@Suppress("UNCHECKED_CAST")
object Persistors {

    val LOG_TAG = "PERSISTORS"

    // controlla se la classe che si sta gestendo appartiene ad una di quelle per cui sono state implementati
    // dei handler particolari
    private fun <T> customHandler(
        isAutoUpdatable: Boolean,
        targetType: Type,
        id: String,
        observer: PersistenceObserver<T>
    ): AbstractPersistenceHandler<T, *>? {
        var handler: AbstractPersistenceHandler<T, *>? = null

        val targetClass = targetType.extractClass()

        if (LiveData::class.java.isAssignableFrom(targetClass)) {
            handler = liveDataHandler(
                isAutoUpdatable,
                targetClass as Class<LiveData<Any?>>,
                targetType as ParameterizedType,
                id, observer as PersistenceObserver<LiveData<Any?>>
            ) as AbstractPersistenceHandler<T, *>
        }

        if (PersistenceAware::class.java.isAssignableFrom(targetClass)) {
            handler = persistenceAwareHandler(
                id, handler as AbstractPersistenceHandler<PersistenceAware, *>?
            ) as AbstractPersistenceHandler<T, *>
        }

        return handler
    }

    private fun <T, L : LiveData<T>> liveDataHandler(
        isAutoUpdatable: Boolean,
        targetClass: Class<L>,
        targetType: ParameterizedType,
        id: String,
        observer: PersistenceObserver<L>
    ): AbstractPersistenceHandler<L, T> {
        Log.d(LOG_TAG, "Persisted type $id is a Live Data, providing custom handling.")


        val innerType = targetType.actualTypeArguments[0].extractClass() as Class<T>

        val innerHandler = customHandler(
            isAutoUpdatable, innerType, id,
            object : PersistenceObserver<T> {}) as PersistenceHandler<T>?

        if (!LiveDataPersistenceObserver::class.java.isAssignableFrom(observer::class.java)) {
            Log.e(
                LOG_TAG,
                "Persisted value is LiveData but the observer is not an instance " +
                        "of ${LiveDataPersistenceObserver::class.java.simpleName}" +
                        " ad will no handle live data changing! Consider changing your observer type."
            )
            return LiveDataPersistenceHandler(
                isAutoUpdatable, targetClass,
                innerType, nextHandler = innerHandler
            )
        } else {
            return LiveDataPersistenceHandler(
                isAutoUpdatable, targetClass, innerType,
                observer as LiveDataPersistenceObserver<T>,
                nextHandler = innerHandler
            )
        }
    }

    private fun <T : PersistenceAware> persistenceAwareHandler(
        id: String,
        handler: AbstractPersistenceHandler<T, *>?
    ): AbstractPersistenceHandler<T, T> {
        Log.d(LOG_TAG, "Persisted type $id is a Persistence Aware, providing custom handling.")
        return PersistenceAwarePersistenceHandler(handler as PersistenceHandler<T>?)
    }

    // i persistors vengono creati tramite il provider in modo da avere thisRef e Property.
    // con istanziazione diretta non vengono passati dal compilatore.
    private fun <T, C> createThroughProvider(
        isAutoUpdatable: Boolean,
        default: T,
        observer: PersistenceObserver<T>,
        targetExtractor: (KProperty<*>) -> Class<T> = { it.type().java as Class<T> },
        creator: (String, C, Class<T>, AbstractPersistenceHandler<T, *>?) -> SimplePersistor<T, C>
    ): PropertyDelegateProvider<C, SimplePersistor<T, C>> {
        return PropertyDelegateProvider { thisRef, property ->
            val targetClass = targetExtractor(property)
            val id = computeId(property)
            creator(
                id, thisRef, targetClass,
                customHandler(
                    isAutoUpdatable,
                    property.returnType.javaType, id, observer
                )
            )
        }
    }


    private fun computeId(property: KProperty<*>): String =
        "${property.type().java.simpleName}.${property.name}"


    fun <T> sharedPreferences(
        default: T,
        typeReference: TypeReference<T>,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<SharedPreferencesPersistableContainer, Persistor<T, SharedPreferencesPersistableContainer>> {
        return createThroughProvider(true, default, observer)
        { id, container, targetClass, handler ->
            SharedPreferencesPersistorDelegate(
                container, id, typeReference, targetClass, default,
                observer, handler
            )
        }
    }

    inline fun <reified T> sharedPreferences(
        default: T,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ) = sharedPreferences(default, object : TypeReference<T>() {}, observer)


    fun <T, C> liveFirestoreCollection(
        collection: String? = null,
        default: T,
        typeReference: TypeReference<T>,
        observer: FirestoreLivePersistenceObserver<QuerySnapshot, T> = object :
            FirestoreLivePersistenceObserver<QuerySnapshot, T> {},
    ): PropertyDelegateProvider<C, Persistor<T, C>> {
        return createThroughProvider(false, default, observer)
        { id, container, targetClass, handler ->
            FirestoreLiveCollectionPersistorDelegate(
                container, id, collection,
                targetClass, typeReference, default, observer, handler
            )
        }
    }

    inline fun <reified T, C> liveFirestoreCollection(
        collection: String? = null,
        default: T,
        observer: FirestoreLivePersistenceObserver<QuerySnapshot, T> = object :
            FirestoreLivePersistenceObserver<QuerySnapshot, T> {},
    ): PropertyDelegateProvider<C, Persistor<T, C>> =
        liveFirestoreCollection(collection, default, object : TypeReference<T>() {}, observer)


    fun <T, C> liveFirestoreMap(
        collection: String? = null,
        default: T,
        typeReference: TypeReference<T>,
        observer: FirestoreLivePersistenceObserver<QuerySnapshot, T> = object :
            FirestoreLivePersistenceObserver<QuerySnapshot, T> {}
    ): PropertyDelegateProvider<C, Persistor<T, C>> {
        return createThroughProvider(true, default, observer)
        { id, container, targetClass, handler ->
            FirestoreLiveMapPersistorDelegate(
                container, id, collection,
                targetClass, typeReference, default, observer, handler
            )
        }
    }

    inline fun <reified T, C> liveFirestoreMap(
        collection: String? = null,
        default: T,
        observer: FirestoreLivePersistenceObserver<QuerySnapshot, T> = object :
            FirestoreLivePersistenceObserver<QuerySnapshot, T> {}
    ): PropertyDelegateProvider<C, Persistor<T, C>> =
        liveFirestoreMap(collection, default, object : TypeReference<T>() {}, observer)


    fun <T, C> simpleLiveFirestore(
        collection: String? = null,
        document: String? = null,
        default: T,
        observer: FirestoreLivePersistenceObserver<DocumentSnapshot, T> = object :
            FirestoreLivePersistenceObserver<DocumentSnapshot, T> {},
    ): PropertyDelegateProvider<C, Persistor<T, C>> {
        return createThroughProvider(false, default, observer)
        { id, container, targetClass, handler ->
            FirestoreLivePersistorDelegate(
                container, id, collection, document,
                targetClass, default, observer, handler
            )
        }
    }


    inline fun <reified T, C> liveFirestore(
        collection: String? = null,
        document: String? = null,
        default: T,
        observer: FirestoreLivePersistenceObserver<Any?, T> = object :
            FirestoreLivePersistenceObserver<Any?, T> {},
    ): PropertyDelegateProvider<C, Persistor<T, C>> =
        object : TypeReference<T>() {}.toJavaType().let { type ->
            when {
                isEligibleAsFirestoreCollection(document, type) ->
                    liveFirestoreCollection(
                        collection, default,
                        observer as FirestoreLivePersistenceObserver<QuerySnapshot, T>
                    )
                isEligibleAsFirestoreMap(document, type) ->
                    liveFirestoreMap(
                        collection, default,
                        observer as FirestoreLivePersistenceObserver<QuerySnapshot, T>
                    )
                else -> simpleLiveFirestore(
                    collection, document, default,
                    observer as FirestoreLivePersistenceObserver<DocumentSnapshot, T>
                )
            }
        }

    fun isEligibleAsFirestoreCollection(document: String?, targetType: JavaType): Boolean {
        if (document != null) return false
        return isEligibleAs(MutableCollection::class.java, targetType, 0)
    }

    fun isEligibleAsFirestoreMap(document: String?, targetType: JavaType): Boolean {
        if (document != null) return false
        return isEligibleAs(MutableMap::class.java, targetType, 1)
    }

    private fun <T> isEligibleAs(
        targetHandledType: Class<T>,
        type: JavaType,
        genericSubParamIndex: Int
    ): Boolean {
        if (targetHandledType.isAssignableFrom(type.rawClass)) {
            val innerType = type.bindings.typeParameters[genericSubParamIndex].rawClass
            return Identifiable::class.java.isAssignableFrom(innerType)
        }
        if (LiveData::class.java.isAssignableFrom(type.rawClass)) {
            return isEligibleAs(
                targetHandledType,
                type.bindings.typeParameters[0],
                genericSubParamIndex
            )
        }
        return false
    }


}

