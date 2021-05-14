package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.google.firebase.firestore.DocumentSnapshot
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver

class FirestoreLiveMapPersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    private val collection: String? = null,
    targetClass: Class<T>,
    targetTypeReference: TypeReference<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?,
) : AbstractFirestoreMultiValuePersistorDelegate<MutableMap<String, Identifiable?>, T, C>(
    thisRef, id, collection, targetClass, targetTypeReference,
    MutableMap::class.java as Class<MutableMap<String, Identifiable?>>,
    1, default, observer, handler
) {


    init {
        initialized()
    }


    override fun parseValues(
        clazz: Class<MutableMap<String, Identifiable?>>,
        q: Collection<DocumentSnapshot>
    ): MutableMap<String, Identifiable?>? {
        Log.i(LOG_TAG, "Parsing values loaded from firestore for $id")
        return if (q.isEmpty()) null else
            q.map {
                it.id to parseNullableValue(it)
            }.toMap(tryCreateDataStructure(clazz))
    }

    override fun tryCreateDataStructure(clazz: Class<MutableMap<String, Identifiable?>>) =
        clazz.constructors[0].newInstance() as MutableMap<String, Identifiable?>


    override fun <R> doLoadPersistence(targetClass: Class<R>): R? =
        //if cast fails just throw up
        parseValues(targetClass as Class<MutableMap<String, Identifiable?>>, toParse) as R


    override fun <R> doPersist(value: R) {
        Log.i(LOG_TAG, "Persisting values for $id on firestore")
        value as Map<String, Identifiable?> // if cast fails just throw up
        value.forEach { k, v -> insert(k, v) }
    }

}