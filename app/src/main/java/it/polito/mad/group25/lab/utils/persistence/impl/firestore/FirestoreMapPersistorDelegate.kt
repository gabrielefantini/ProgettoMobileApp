package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler

class FirestoreMapPersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    private val collection: String? = null,
    targetClass: Class<T>,
    default: T,
    private val mapBuilder: (DocumentSnapshot, Class<MutableMap<Any?, Any?>>) -> Pair<Any?, Any?>,
    private val entriesSaver: (Pair<Any?, Any?>, CollectionReference) -> Unit,
    observer: FirestoreLivePersistenceObserver<QuerySnapshot, T>,
    handler: AbstractPersistenceHandler<T, *>?,
) : AbstractFirestoreMultiValuePersistorDelegate<T, C>(
    thisRef, id, collection, targetClass,
    default, observer, handler
) {

    init {
        initialized()
    }

    private fun <M : MutableMap<Any?, Any?>> parseValues(
        clazz: Class<M>,
        q: QuerySnapshot?
    ): M? {
        Log.i(LOG_TAG, "Parsing values loaded from firestore for $id")
        return q?.documents?.mapNotNull {
            this.mapBuilder(it, clazz as Class<MutableMap<Any?, Any?>>)
        }?.toMap(tryCreateMap(clazz))
    }

    private fun <M : MutableMap<Any?, Any?>>
            tryCreateMap(clazz: Class<M>): M =
        clazz.constructors[0].newInstance() as M


    override fun <R> doLoadPersistence(targetClass: Class<R>): R? =
        if (isParsable())
            parseValues(targetClass as Class<MutableMap<Any?, Any?>>, toParse) as R
        else null


    override fun <R> doPersist(value: R) {
        Log.i(LOG_TAG, "Persisting values for $id on firestore")
        value as Map<Any?, Any?>
        value.forEach { k, v -> entriesSaver(k to v, store) }
    }

}