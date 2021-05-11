package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group25.lab.AuthenticationContext
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor

interface FirestoreLivePersistenceObserver<SF, T> :
    PersistenceObserver<T> {

    /**
     * Intercepts an async value receiving. Has to handle the eventual error or to interrupt the processing.
     */
    fun onAsyncValueReceived(value: SF?, error: Exception?) {
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
) : SimplePersistor<T, C>(
    thisRef, id, targetClass,
    default, observer, handler
) {

    lateinit var toParse: DocumentSnapshot

    protected val NULL_VALUE = "null"


    var store: DocumentReference

    init {
        // Prendo la collection data oppure il l'id del field in questione
        // Prendo il documento dato oppure quello che appartiene all'utente selezionato.
        // Esempio pratico per gli utenti: Collection di utenti in cui l'oggetto che mi interessa è quello dell'utente x.
        val c = collection ?: id
        val d = document ?: AuthenticationContext.userID
        Log.i(LOG_TAG, "${javaClass.simpleName} will try using $c/$d for $id")
        store = FirebaseFirestore.getInstance()
            .collection(c)
            .document(d)

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
        store.set(value ?: NULL_VALUE)
    }

}




