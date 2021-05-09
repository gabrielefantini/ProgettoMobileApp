package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.genericType
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver

abstract class FirestoreLivePersistenceObserver<SF, Q, T : MutableLiveData<Q>> :
    PersistenceObserver<T> {

    /**
     * Intercepts an async value receiving. Has to handle the eventual error or to interrupt the processing.
     */
    open fun onAsyncValueReceived(value: SF?, error: FirebaseFirestoreException?) {
        if (error != null)
            throw error
    }

    /**
     * Intercepts the instant before the live data assignment. Has to return the value to be assigned.
     */
    open fun beforeLiveDataUpdate(oldValue: Q?, newValue: Q): Q? = newValue

    /**
     * Intercepts the instant after the live data assignment.
     */
    open fun afterLiveDataUpdate(value: Q?) {}

}

class FirestoreLivePersistorDelegate<Q, T : MutableLiveData<Q>, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    document: String? = null,
    targetClass: Class<T>,
    default: T,
    observer: FirestoreLivePersistenceObserver<DocumentSnapshot, Q, T>,
    handler: AbstractPersistenceHandler<T, *>?
) : AbstractFirestorePersistorDelegate<T, C>(
    thisRef, id, collection, document,
    targetClass, default, observer, handler
) {

    init {
        store.addSnapshotListener { value, error ->
            observer.onAsyncValueReceived(value, error)
            parseNullableValue(targetClass.genericType() as Class<Q>, value)?.let {
                this.value.value = observer.beforeLiveDataUpdate(this.value.value, it)
                observer.afterLiveDataUpdate(this.value.value)
            }
        }
    }

    //persistency is loaded by the snapshot listener
    override fun <R> doLoadPersistence(targetClass: Class<R>): R? = null

    override fun <R> doPersist(value: R) = doPersistNullableValue(value)

}


class FirestoreLiveCollectionPersistorDelegate<Q, T : MutableCollection<Q>, L : MutableLiveData<T>, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    targetClass: Class<L>,
    default: L,
    observer: FirestoreLivePersistenceObserver<QuerySnapshot, T, L>,
    handler: AbstractPersistenceHandler<L, *>?
) : AbstractFirestoreCollectionPersistorDelegate<L, C>(
    thisRef, id, collection,
    targetClass, default, observer, handler
) {

    init {
        store.addSnapshotListener { value, error ->
            observer.onAsyncValueReceived(value, error)
            parseValues(targetClass.genericType() as Class<MutableCollection<Any?>>, value)?.let {
                this.value.value = observer.beforeLiveDataUpdate(this.value.value, it as T)
                observer.afterLiveDataUpdate(this.value.value)
            }
        }
    }

    override fun <R> doPersist(value: R) = doPersistValues(value as Collection<*>)

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? = null


}

