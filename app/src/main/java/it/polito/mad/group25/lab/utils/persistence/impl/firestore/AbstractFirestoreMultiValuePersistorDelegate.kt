package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
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
    protected var toParse: Collection<DocumentSnapshot> = mutableListOf()
    protected val justStoredCache = FirestoreJustInsertedCache(id)

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
            toParse = value.documents.filter {
                !justStoredCache.contains(it.id)
                    .apply {
                        if (this)
                            Log.d(LOG_TAG, "Ignoring just persisted value with id ${it.id}")
                    }
            }
            loadPersistence()?.let { this.value = it } //trigger the handlers
        }
    }

    protected fun handleInsertion(insertionTask: Task<DocumentReference>) {
        insertionTask.addOnCompleteListener {
            if (it.isSuccessful)
                justStoredCache.add(it.result!!.id)
        }
    }

}