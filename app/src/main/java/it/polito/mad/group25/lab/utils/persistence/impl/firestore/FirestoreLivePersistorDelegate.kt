package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor

interface FirestoreLivePersistenceObserver<SF, T> {
    /**
     * Intercepts an async value receiving. Has to handle the eventual error or to interrupt the processing.
     */
    fun onAsyncValueReceived(value: SF?, error: Exception?) {
        if (error != null)
            throw error
    }
}


@Suppress("UNCHECKED_CAST")
class FirestoreLivePersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    private var collection: String? = null,
    private var document: String? = null,
    private var lazyInit: Boolean,
    targetClass: Class<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?
) : SimplePersistor<T, C>(
    thisRef, id, targetClass,
    default, observer, handler
) {


    private val NULL_VALUE = "null"

    private lateinit var toParse: DocumentSnapshot
    private lateinit var store: DocumentReference
    private lateinit var listenerRegistration: ListenerRegistration

    init {
        // Prendo la collection data oppure il l'id del field in questione
        // Prendo il documento dato oppure quello che appartiene all'utente selezionato.
        // Esempio pratico per gli utenti: Collection di utenti in cui l'oggetto che mi interessa è quello dell'utente x.
        collection = collection ?: id
        if (!lazyInit) {
            Log.d(LOG_TAG, "${javaClass.simpleName} is not in lazy mode for $id")
            if (document == null)
                throw IllegalStateException("${javaClass.simpleName} is not on lazy initialization mode and none document id was provided")
            initializeStore(document!!)
        } else {
            Log.d(LOG_TAG, "${javaClass.simpleName} is in lazy mode for $id")
        }
        initialized()
    }

    private fun <R> parseNullableValue(clazz: Class<R>, doc: DocumentSnapshot): R? {
        Log.i(LOG_TAG, "Parsing value loaded from firestore for $id")
        return doc.let {
            if (it.toString() != NULL_VALUE) it.toObject(clazz) as R else null
        }
    }


    override fun <R> doLoadPersistence(targetClass: Class<R>): R? =
        if (this::toParse.isInitialized) parseNullableValue(targetClass, toParse) else null

    override fun <R> doPersist(value: R) {
        Log.i(LOG_TAG, "Persisting value for $id on firestore")
        if (value is Identifiable) {
            if (value.id == null) {
                FirebaseFirestore.getInstance()
                    .collection(this.collection!!).add(value)
                    .addOnCompleteListener { t ->
                        if (t.isSuccessful) value.id = t.result!!.id
                    }
            } else {
                if (!this::store.isInitialized)
                    initializeStore(value.id!!)
                if (this.store.id != value.id!!)
                    throw Error(
                        "Referencing a document with id ${store.id} " +
                                "but trying to persist an object with id ${value.id!!}"
                    )
                store.set(value).addOnCompleteListener {
                    if (!it.isSuccessful)
                        observer.handleGenericException(it.exception!!)
                }
            }
        } else {
            if (!this::store.isInitialized)
                throw Error("Trying to persist an object which is not identifiable with a non initialized store")
            store.set(value ?: NULL_VALUE)
        }
    }

    private fun initializeStore(document: String) {
        Log.i(LOG_TAG, "${javaClass.simpleName} will try using $collection/$document for $id")
        store = FirebaseFirestore.getInstance()
            .collection(this.collection!!)
            .document(document)

        listenerRegistration = store.addSnapshotListener { value, error ->
            Log.i(LOG_TAG, "Received async value for $id.")

            if (value?.metadata?.hasPendingWrites() == true) {
                Log.i(LOG_TAG, "Ignored async value received since it's a local modification")
                return@addSnapshotListener
            }

            if (observer is FirestoreLivePersistenceObserver<*, *>) {
                (observer as FirestoreLivePersistenceObserver<DocumentSnapshot, T>)
                    .onAsyncValueReceived(value, error)
                if (error != null) return@addSnapshotListener
            }

            if (value == null)
                throw IllegalArgumentException(
                    "Received value is null! " +
                            "This should never happen, check error handling of the observer!"
                )
            toParse = value
            loadPersistenceAndSaveIt()
        }
    }

    fun loadAnotherDocument(document: String): DocumentReference {
        if (this::listenerRegistration.isInitialized)
            listenerRegistration.remove()
        initializeStore(document)
        return store
    }

}




