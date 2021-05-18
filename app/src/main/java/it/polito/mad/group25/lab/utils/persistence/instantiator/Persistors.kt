package it.polito.mad.group25.lab.utils.persistence.instantiator

import com.fasterxml.jackson.core.type.TypeReference
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.impl.SharedPreferencesPersistableContainer
import it.polito.mad.group25.lab.utils.persistence.impl.SharedPreferencesPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreDocumentChanger
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLiveCollectionPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLiveMapPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistorDelegate
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
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
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
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<C, FirestoreLiveCollectionPersistorDelegate<T, C>> =
        liveFirestoreCollection(collection, default, id, object : TypeReference<T>() {}, observer)


    fun <T, C> liveFirestoreMap(
        collection: String? = null,
        default: T,
        id: String? = null,
        typeReference: TypeReference<T>,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
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
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<C, FirestoreLiveMapPersistorDelegate<T, C>> =
        liveFirestoreMap(collection, default, id, object : TypeReference<T>() {}, observer)


    fun <T, C> simpleLiveFirestore(
        collection: String? = null,
        document: String? = null,
        lazyInit: Boolean = false,
        documentChanger: FirestoreDocumentChanger<T> = FirestoreDocumentChanger(),
        default: T,
        id: String? = null,
        typeReference: TypeReference<T>,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<C, FirestoreLivePersistorDelegate<T, C>> {
        return PersistorInstantiator.createThroughProvider(typeReference, default, id, observer)
        { id, container, targetClass, handler ->
            FirestoreLivePersistorDelegate(
                container, id, collection, document, lazyInit, documentChanger,
                targetClass, default, observer, handler
            )
        }
    }

    inline fun <reified T, C> simpleLiveFirestore(
        collection: String? = null,
        document: String? = null,
        lazyInit: Boolean = false,
        documentChanger: FirestoreDocumentChanger<T> = FirestoreDocumentChanger(),
        default: T,
        id: String? = null,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<C, FirestoreLivePersistorDelegate<T, C>> =
        simpleLiveFirestore(
            collection, document, lazyInit, documentChanger,
            default, id, object : TypeReference<T>() {}, observer
        )


}

