package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver

abstract class FirestoreLivePersistenceObserver<SF, T> :
    PersistenceObserver<T> {

    /**
     * Intercepts an async value receiving. Has to handle the eventual error or to interrupt the processing.
     */
    open fun onAsyncValueReceived(value: SF?, error: FirebaseFirestoreException?) {
        if (error != null)
            throw error
    }

}

class FirestoreLivePersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    document: String? = null,
    targetClass: Class<T>,
    default: T,
    observer: FirestoreLivePersistenceObserver<DocumentSnapshot, T>,
    handler: AbstractPersistenceHandler<T, *>?
) : AbstractFirestorePersistorDelegate<T, C>(
    thisRef, id, collection, document,
    targetClass, default, observer, handler
) {

    lateinit var toParse: DocumentSnapshot

    init {
        store.addSnapshotListener { value, error ->
            Log.i(LOG_TAG, "Received async value for $id.")
            observer.onAsyncValueReceived(value, error)
            if (value == null)
                throw IllegalArgumentException(
                    "Received value is null! " +
                            "This should never happen, check error handling of the observer!"
                )
            toParse = value
            loadPersistence()?.let { this.value = it } //trigger the handlers
        }
        initialized()
    }

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? =
        if (this::toParse.isInitialized) parseNullableValue(targetClass, toParse) else null

    override fun <R> doPersist(value: R) = doPersistNullableValue(value)

}


class FirestoreLiveCollectionPersistorDelegate<Q, T : MutableCollection<Q>, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    targetClass: Class<T>,
    default: T,
    observer: FirestoreLivePersistenceObserver<QuerySnapshot, T>,
    handler: AbstractPersistenceHandler<T, *>?
) : AbstractFirestoreCollectionPersistorDelegate<T, C>(
    thisRef, id, collection,
    targetClass, default, observer, handler
) {

    lateinit var toParse: QuerySnapshot

    init {
        store.addSnapshotListener { value, error ->
            Log.i(LOG_TAG, "Received async value for $id.")
            observer.onAsyncValueReceived(value, error)
            if (value == null)
                throw IllegalArgumentException(
                    "Received value is null! " +
                            "This should never happen, check error handling of the observer!"
                )
            toParse = value
            loadPersistence()?.let { this.value = it } //trigger the handlers
        }
        initialized()
    }


    override fun <R> doPersist(value: R) = doPersistValues(value as Collection<*>)

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? =
        if (this::toParse.isInitialized)
            parseValues(targetClass as Class<MutableCollection<Any?>>, toParse) as R
        else null


}

