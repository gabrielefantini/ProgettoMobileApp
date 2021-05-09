package it.polito.mad.group25.lab.utils.persistence

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.core.type.TypeReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.genericType
import it.polito.mad.group25.lab.utils.persistence.impl.*
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.*
import it.polito.mad.group25.lab.utils.type
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

//creato per separare la logica di scelta del persistor dal persistor stesso
@Suppress("UNCHECKED_CAST")
object Persistors {

    val LOG_TAG = "PERSISTORS"

    // controlla se la classe che si sta gestendo appartiene ad una di quelle per cui sono state implementati
    // dei handler particolari
    private fun <T> customHandler(
        isAutoUpdatable: Boolean,
        targetClass: Class<T>,
        id: String,
        observer: PersistenceObserver<T>
    ): AbstractPersistenceHandler<T, *>? {
        var handler: AbstractPersistenceHandler<T, *>? = null

        if (LiveData::class.java.isAssignableFrom(targetClass)) {
            Log.d(LOG_TAG, "Persisted type $id is a Live Data, providing custom handling.")
            val innerType = object : TypeReference<T>() {}.genericType()

            if (!LiveDataPersistenceObserver::class.java.isAssignableFrom(observer::class.java)) {
                Log.e(
                    LOG_TAG,
                    "Persisted value is LiveData but the observer is not an instance " +
                            "of ${LiveDataPersistenceObserver::class.java.simpleName}" +
                            " ad will no handle live data changing! Consider changing your observer type."
                )
                handler = LiveDataPersistenceHandler(
                    isAutoUpdatable,
                    targetClass as Class<LiveData<Any?>>,
                    innerType as Class<Any?>,
                    nextHandler = handler as PersistenceHandler<Any?>
                ) as AbstractPersistenceHandler<T, *>
            } else {
                return LiveDataPersistenceHandler(
                    isAutoUpdatable,
                    targetClass as Class<LiveData<Any?>>,
                    innerType as Class<Any?>,
                    observer as LiveDataPersistenceObserver<Any?>,
                    handler as PersistenceHandler<Any?>
                ) as AbstractPersistenceHandler<T, *>
            }
        }

        if (PersistenceAware::class.java.isAssignableFrom(targetClass)) {
            Log.d(LOG_TAG, "Persisted type $id is a Persistence Aware, providing custom handling.")
            handler =
                PersistenceAwarePersistenceHandler(handler as PersistenceHandler<PersistenceAware>)
                        as AbstractPersistenceHandler<T, *>
        }

        return handler
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
                customHandler(isAutoUpdatable, targetClass, id, observer)
            )
        }
    }


    private fun computeId(property: KProperty<*>): String =
        "${property.type().java.simpleName}.${property.name}"


    fun <T> sharedPreferences(
        default: T,
        typeReference: TypeReference<T>? = null,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<SharedPreferencesPersistableContainer, Persistor<T, SharedPreferencesPersistableContainer>> {
        return createThroughProvider(true, default, observer)
        { id, container, targetClass, handler ->
            SharedPreferencesPersistorDelegate(
                container, id, targetClass, default,
                observer, handler
            )
        }
    }

    fun <Q, T : MutableCollection<Q>, L : MutableLiveData<T>, C> liveFirestoreCollection(
        collection: String? = null,
        default: L,
        observer: FirestoreLivePersistenceObserver<QuerySnapshot, T, L> = object :
            FirestoreLivePersistenceObserver<QuerySnapshot, T, L>() {},
    ): PropertyDelegateProvider<C, Persistor<L, C>> {
        return createThroughProvider(false, default, observer)
        { id, container, targetClass, handler ->
            FirestoreLiveCollectionPersistorDelegate(
                container, id, collection,
                targetClass, default, observer, handler
            )
        }
    }

    fun <T, L : MutableLiveData<T>, C> liveFirestore(
        collection: String? = null,
        document: String? = null,
        default: L,
        observer: FirestoreLivePersistenceObserver<DocumentSnapshot, T, L> = object :
            FirestoreLivePersistenceObserver<DocumentSnapshot, T, L>() {},
    ): PropertyDelegateProvider<C, Persistor<L, C>> {
        return createThroughProvider(false, default, observer)
        { id, container, targetClass, handler ->
            FirestoreLivePersistorDelegate(
                container, id, collection, document,
                targetClass, default, observer, handler
            )
        }
    }

    fun <E, T : MutableCollection<E>, C> firestoreCollection(
        collection: String? = null,
        default: T,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<C, Persistor<T, C>> {
        return createThroughProvider(true, default, observer)
        { id, container, targetClass, handler ->
            FirestoreCollectionPersistorDelegate(
                container, id, collection,
                targetClass, default, observer, handler
            )
        }
    }

    fun <T, C> simpleFirestore(
        collection: String? = null,
        document: String? = null,
        default: T,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<C, Persistor<T, C>> {
        return createThroughProvider(true, default, observer)
        { id, container, targetClass, handler ->
            FirestorePersistorDelegate(
                container, id, collection, document, targetClass,
                default, observer, handler
            )
        }
    }

    inline fun <reified T, C> firestore(
        collection: String? = null,
        document: String? = null,
        default: T,
        observer: PersistenceObserver<T> = object : PersistenceObserver<T> {}
    ): PropertyDelegateProvider<C, Persistor<T, C>> {
        val isLive = MutableLiveData::class.java.isAssignableFrom(T::class.java)
        val requiresWholeCollection =
            Collection::class.java.isAssignableFrom(if (isLive) T::class.java.genericType() else T::class.java)

        if (isLive) {
            val liveObserver: FirestoreLivePersistenceObserver<Any?, Any?, MutableLiveData<Any?>>
            if (!FirestoreLivePersistenceObserver::class.java.isAssignableFrom(observer::class.java)) {
                Log.e(
                    LOG_TAG,
                    "Will provide live firestore persistor but the observer is not an instance " +
                            "of ${FirestoreLivePersistenceObserver::class.java.simpleName}" +
                            " ad will no handle live data changing! Consider changing your observer type."
                )
                liveObserver =
                    object :
                        FirestoreLivePersistenceObserver<Any?, Any?, MutableLiveData<Any?>>() {}
            } else liveObserver =
                observer as FirestoreLivePersistenceObserver<Any?, Any?, MutableLiveData<Any?>>
            if (requiresWholeCollection)

        }

    }

}

