package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor

abstract class AbstractFirestoreMultiValuePersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    private val collection: String? = null,
    targetClass: Class<T>,
    default: T,
    observer: FirestoreLivePersistenceObserver<QuerySnapshot, T>,
    handler: AbstractPersistenceHandler<T, *>?,
) : SimplePersistor<T, C>(thisRef, id, targetClass, default, observer, handler) {

    var store: CollectionReference
    lateinit var toParse: QuerySnapshot

    init {
        val collection = collection ?: id
        Log.i(LOG_TAG, "${javaClass.simpleName} will try using $collection for $id")
        store = FirebaseFirestore.getInstance().collection(collection)
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
    }

    protected fun isParsable() = this::toParse.isInitialized
}