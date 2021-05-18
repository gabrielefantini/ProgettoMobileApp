package it.polito.mad.group25.lab.utils.persistence.impl.firestore.observers

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.LiveDataPersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.PersistorAware
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistorDelegate

class OnValueChangesLoadDocumentFirestoreObserver<T : Identifiable?>
    : PersistenceObserver<T>,
    FirestoreLivePersistenceObserver<DocumentSnapshot, T>,
    PersistorAware<T, Any?, FirestoreLivePersistorDelegate<T, Any?>> {

    private lateinit var persistor: FirestoreLivePersistorDelegate<T, Any?>

    override fun setPersistor(persistor: FirestoreLivePersistorDelegate<T, Any?>) {
        this.persistor = persistor
    }

    override fun beforeValueChanges(oldValue: T, newValue: T): T? =
        super.beforeValueChanges(oldValue, newValue)
            ?.apply { this.id?.let { persistor.documentChanger.changeDocument(it) } }

    override fun beforePerformingPersistence(value: T): T? = null //do not persist

}

class OnLiveDataValueChangesLoadDocumentFirestoreObserver<T : Identifiable?, L : LiveData<T>>
    : PersistenceObserver<L>,
    FirestoreLivePersistenceObserver<DocumentSnapshot, L>,
    LiveDataPersistenceObserver<T>,
    PersistorAware<L, Any?, FirestoreLivePersistorDelegate<L, Any?>> {

    private lateinit var persistor: FirestoreLivePersistorDelegate<L, Any?>

    override fun setPersistor(persistor: FirestoreLivePersistorDelegate<L, Any?>) {
        this.persistor = persistor
    }

    override fun beforeValueChanges(oldValue: L, newValue: L): L {
        if (oldValue == newValue) return newValue
        newValue.value?.id?.let { persistor.documentChanger.changeDocument(it) }
        newValue.observeForever {
            if (it?.id != null) {
                persistor.documentChanger.changeDocument(it.id!!)
            }
        }
        return newValue
    }

    override fun onLiveValueChanges(newValue: T) {
        persistor.documentChanger.changeDocument(newValue?.id ?: "null")
    }

    override fun beforePerformingPersistence(value: L): L? = null //don't persist
    override fun beforePerformingLiveValuePersistency(value: T): T? = null //do not persist

}