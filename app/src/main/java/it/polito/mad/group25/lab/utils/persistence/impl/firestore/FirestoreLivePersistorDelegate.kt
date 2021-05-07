package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import it.polito.mad.group25.lab.utils.persistence.PersistencyObserver
import kotlin.reflect.KProperty

abstract class FirestoreLivePersistencyObserver<Q, T : MutableLiveData<Q>> :
    PersistencyObserver<T>() {

    /**
     * Intercepts an async value receiving. Has to handle the eventual error or to interrupt the processing.
     */
    open fun onAsyncValueReceived(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
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
    innerTargetType: Class<Q>,
    thisRef: C,
    property: KProperty<*>,
    collection: String? = null,
    document: String? = null,
    default: T,
    observer: FirestoreLivePersistencyObserver<Q, T> = object :
        FirestoreLivePersistencyObserver<Q, T>() {}
) : FirestorePersistorDelegate<T, C>(
    thisRef,
    property,
    collection,
    document,
    default,
    observer
) {

    init {
        store.addSnapshotListener { value, error ->
            observer.onAsyncValueReceived(value, error)
            doLoadNullableValue(innerTargetType, value)?.let {
                this.value.value = observer.beforeLiveDataUpdate(this.value.value, it)
                observer.afterLiveDataUpdate(this.value.value)
            }
        }
    }

    //non serve leggere niente con questo metodo, l'aggiornamento è asincrono.
    override fun doLoadPersistence(): T = default

    //scrivi il valore dentro il live data.
    override fun doPersist(value: T) = doPersistNullableValue(value.value)

}
