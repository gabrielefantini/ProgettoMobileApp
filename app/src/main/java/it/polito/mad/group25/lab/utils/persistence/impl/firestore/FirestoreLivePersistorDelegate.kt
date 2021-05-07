package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlin.reflect.KProperty


class FirestoreLivePersistorDelegate<T : MutableLiveData<Any>, C>(
    thisRef: C,
    property: KProperty<*>,
    collection: String? = null,
    document: String? = null,
    default: T,
    protected val onError: (FirebaseFirestoreException) -> Boolean = { throw it },
    onValueChanges: (T) -> Unit = {},
    onLoadedPersistedValue: (T) -> Unit = {},
) : FirestorePersistorDelegate<T, C>(
    thisRef,
    property,
    collection,
    document,
    default,
    onValueChanges,
    onLoadedPersistedValue
) {

    init {
        store.addSnapshotListener { value, error ->
            if (error != null)
                if (!onError(error))
                    return@addSnapshotListener

            this.value?.let {
                it.value = value?.toObject(targetClass) as T
            }
        }
    }

    override fun loadPersistence(): T = default

}
