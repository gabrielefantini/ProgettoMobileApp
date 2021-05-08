package it.polito.mad.group25.lab.utils.persistence

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.core.type.TypeReference
import it.polito.mad.group25.lab.utils.persistence.impl.*
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistencyObserver
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestorePersistorDelegate
import it.polito.mad.group25.lab.utils.type
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSuperclassOf

@Suppress("UNCHECKED_CAST")
object Persistors {

    // i persistors vengono creati tramite il provider in modo da avere thisRef e Property.
    // con istanziazione diretta non vengono passati dal compilatore.
    private fun <T, C> createThroughProvider(
        default: T,
        targetExtractor: (KProperty<*>) -> Class<T> = { it.type().java as Class<T> },
        creator: (String, C, T, Class<T>) -> SimplePersistor<T, C>
    ): PropertyDelegateProvider<C, SimplePersistor<T, C>> {
        return PropertyDelegateProvider { thisRef, property ->
            var persistor: SimplePersistor<T, C> =
                if (LiveData::class.isSuperclassOf(property.type())) {
                    //crea il persistor richiesto per il tipo interno wrappato dal Live Data
                    // successivamente wrappa quel persistor con quello default di LiveData.
                    assert(default != null) { "Cannot create persistor of LiveData without a valid default value!" }
                    val innerPersistor: SimplePersistor<Any, C> =
                        createPersistorOfInnerType(
                            property,
                            thisRef,
                            default as LiveData<Any>,
                            { it.value!! },
                            { id, c, d, t ->
                                creator(id, c, d as T, t as Class<T>) as SimplePersistor<Any, C>
                            }
                        )
                    wrapLiveData(innerPersistor, default as LiveData<Any>, property)
                } else
                    creator(computeId(property), thisRef, default, targetExtractor(property))

            if (PersistenceAware::class.isSuperclassOf(property.type())) {
                persistor = wrapPersistenceAware(persistor as SimplePersistor<PersistenceAware, C>)
            }

            persistor
        }
    }

    private fun <Q, T, C> wrapLiveData(
        persistor: SimplePersistor<Q, C>,
        default: LiveData<Q>,
        property: KProperty<*>
    ): SimplePersistor<T, C> =
        LiveDataPersistorSubscriber(persistor, default, property) as SimplePersistor<T, C>

    private fun <T, C> wrapPersistenceAware(persistor: SimplePersistor<PersistenceAware, C>): SimplePersistor<T, C> =
        PersistenceAwarePersistor(persistor) as SimplePersistor<T, C>

    //crea un persistor per il tipo interno che un determinato oggetto wrappa. Esempio pratico: crea un persistor per il tipo
    // che il LiveData wrappa.
    private fun <I : Any, T, C> createPersistorOfInnerType(
        property: KProperty<*>,
        thisRef: C,
        upperDefault: T,
        innerDefaultExtractor: (T) -> I,
        creator: (String, C, I, Class<I>) -> SimplePersistor<I, C>
    ): SimplePersistor<I, C> {
        val innerType =
            /*property.type().typeParameters
                .apply {
                    if (size != 1)
                        throw IllegalArgumentException(
                            "Unsupported multiple generic type. Which one has to be handled" +
                                    "in order to create Persistor delegate for $id?"
                        )
                }[0]*/ innerDefaultExtractor(upperDefault)::class.java as Class<I>
        val innerDefault = innerDefaultExtractor(upperDefault)
        return createThroughProvider<I, C>(innerDefault, { innerType }) { id, c, d, f ->
            creator(id, c, d, f)
        }.provideDelegate(thisRef, property)
    }

    private fun computeId(property: KProperty<*>): String =
        "${property.type().java.simpleName}.${property.name}"


    fun <T> sharedPreferences(
        default: T,
        typeReference: TypeReference<T>? = null,
        observer: PersistencyObserver<T> = object : PersistencyObserver<T> {}
    ): PropertyDelegateProvider<SharedPreferencesPersistableContainer, Persistor<T, SharedPreferencesPersistableContainer>> =
        createThroughProvider(default) { id, thisRef, def, targetClass ->
            SharedPreferencesPersistorDelegate(
                thisRef, id, targetClass,
                def, typeReference, observer
            )
        }

    fun <T, C> firestore(
        collection: String? = null,
        document: String? = null,
        default: T,
        observer: PersistencyObserver<T> = object : PersistencyObserver<T>{}
    ): PropertyDelegateProvider<C, Persistor<T, C>> =
        createThroughProvider(default) { id, thisRef, def, targetClass ->
            FirestorePersistorDelegate(
                thisRef, id, collection,
                document, def, targetClass, observer
            )
        }

    fun <Q, T : MutableLiveData<Q>, C> liveFirestore(
        collection: String? = null,
        document: String? = null,
        default: T,
        observer: FirestoreLivePersistencyObserver<Q, T> = object :
            FirestoreLivePersistencyObserver<Q, T>() {}
    ): PropertyDelegateProvider<C, Persistor<T, C>> =
        createThroughProvider(default) { id, thisRef, def, targetClass ->
            FirestoreLivePersistorDelegate(
                (def.value!!::class.java) as Class<Q>, thisRef, id, collection,
                document, def, observer
            )
        }
}

