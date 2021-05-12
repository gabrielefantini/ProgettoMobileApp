package it.polito.mad.group25.lab.utils.persistence.instantiator

import androidx.lifecycle.LiveData
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.Persistor
import it.polito.mad.group25.lab.utils.persistence.impl.SharedPreferencesPersistableContainer
import it.polito.mad.group25.lab.utils.persistence.impl.SharedPreferencesPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLiveCollectionPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLiveMapPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistorDelegate
import it.polito.mad.group25.lab.utils.toJavaType
import kotlin.properties.PropertyDelegateProvider

//creato per separare la logica di scelta del persistor dal persistor stesso
@Suppress("UNCHECKED_CAST")
object Persistors {

    val LOG_TAG = "PERSISTORS"
    
    fun <T> sharedPreferences(
        default: T,
        typeReference: TypeReference<T>,
        id: String? = null,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<SharedPreferencesPersistableContainer, SharedPreferencesPersistorDelegate<T>> {
        return PersistorInstantiator.createThroughProvider(typeReference, default, id, observer)
        { id, container, targetClass, handler ->
            SharedPreferencesPersistorDelegate(
                container, id, typeReference, targetClass, default,
                observer, handler
            )
        }
    }

    inline fun <reified T> sharedPreferences(
        default: T,
        id: String? = null,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ) = sharedPreferences(default, object : TypeReference<T>() {}, id, observer)


    fun <T, C> liveFirestoreCollection(
        collection: String? = null,
        default: T,
        id: String? = null,
        typeReference: TypeReference<T>,
        observer: FirestoreLivePersistenceObserver<QuerySnapshot, T> = object :
            FirestoreLivePersistenceObserver<QuerySnapshot, T> {},
    ): PropertyDelegateProvider<C, FirestoreLiveCollectionPersistorDelegate<T, C>> {
        return PersistorInstantiator.createThroughProvider(typeReference, default, id, observer)
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
        id: String? = null,
        observer: FirestoreLivePersistenceObserver<QuerySnapshot, T> = object :
            FirestoreLivePersistenceObserver<QuerySnapshot, T> {},
    ): PropertyDelegateProvider<C, FirestoreLiveCollectionPersistorDelegate<T, C>> =
        liveFirestoreCollection(collection, default, id, object : TypeReference<T>() {}, observer)


    fun <T, C> liveFirestoreMap(
        collection: String? = null,
        default: T,
        id: String? = null,
        typeReference: TypeReference<T>,
        observer: FirestoreLivePersistenceObserver<QuerySnapshot, T> = object :
            FirestoreLivePersistenceObserver<QuerySnapshot, T> {}
    ): PropertyDelegateProvider<C, FirestoreLiveMapPersistorDelegate<T, C>> {
        return PersistorInstantiator.createThroughProvider(typeReference, default, id, observer)
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
        id: String? = null,
        observer: FirestoreLivePersistenceObserver<QuerySnapshot, T> = object :
            FirestoreLivePersistenceObserver<QuerySnapshot, T> {}
    ): PropertyDelegateProvider<C, FirestoreLiveMapPersistorDelegate<T, C>> =
        liveFirestoreMap(collection, default, id, object : TypeReference<T>() {}, observer)


    fun <T, C> simpleLiveFirestore(
        collection: String? = null,
        document: String? = null,
        lazyInit: Boolean = false,
        default: T,
        id: String? = null,
        typeReference: TypeReference<T>,
        observer: FirestoreLivePersistenceObserver<DocumentSnapshot, T> = object :
            FirestoreLivePersistenceObserver<DocumentSnapshot, T> {},
    ): PropertyDelegateProvider<C, FirestoreLivePersistorDelegate<T, C>> {
        return PersistorInstantiator.createThroughProvider(typeReference, default, id, observer)
        { id, container, targetClass, handler ->
            FirestoreLivePersistorDelegate(
                container, id, collection, document, lazyInit,
                targetClass, default, observer, handler
            )
        }
    }

    inline fun <reified T, C> simpleLiveFirestore(
        collection: String? = null,
        document: String? = null,
        lazyInit: Boolean = false,
        default: T,
        id: String? = null,
        observer: FirestoreLivePersistenceObserver<DocumentSnapshot, T> = object :
            FirestoreLivePersistenceObserver<DocumentSnapshot, T> {},
    ): PropertyDelegateProvider<C, FirestoreLivePersistorDelegate<T, C>> =
        simpleLiveFirestore(
            collection, document, lazyInit,
            default, id, object : TypeReference<T>() {}, observer
        )


    inline fun <reified T, C> liveFirestore(
        collection: String? = null,
        document: String? = null,
        default: T,
        id: String? = null,
        lazyInit: Boolean? = null,
        observer: FirestoreLivePersistenceObserver<Any?, T> = object :
            FirestoreLivePersistenceObserver<Any?, T> {},
    ): PropertyDelegateProvider<C, Persistor<T, C>> =
        object : TypeReference<T>() {}.toJavaType().let { type ->
            when {
                isEligibleAsFirestoreCollection(document, lazyInit, type) ->
                    liveFirestoreCollection(
                        collection, default, id,
                        observer as FirestoreLivePersistenceObserver<QuerySnapshot, T>
                    )
                isEligibleAsFirestoreMap(document, lazyInit, type) ->
                    liveFirestoreMap(
                        collection, default, id,
                        observer as FirestoreLivePersistenceObserver<QuerySnapshot, T>
                    )
                else -> simpleLiveFirestore(
                    collection,
                    document, lazyInit!!,
                    default, id, observer as FirestoreLivePersistenceObserver<DocumentSnapshot, T>
                )
            }
        }

    fun isEligibleAsFirestoreCollection(
        document: String?,
        lazyInit: Boolean?,
        targetType: JavaType
    ): Boolean {
        if (document != null || lazyInit != null) return false
        return isEligibleAs(MutableCollection::class.java, targetType, 0)
    }

    fun isEligibleAsFirestoreMap(
        document: String?, lazyInit: Boolean?,
        targetType: JavaType
    ): Boolean {
        if (document != null || lazyInit != null) return false
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

