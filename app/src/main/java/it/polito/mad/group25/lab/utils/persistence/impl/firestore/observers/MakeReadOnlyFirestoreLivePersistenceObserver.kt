package it.polito.mad.group25.lab.utils.persistence.impl.firestore.observers

import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.extractPersistor
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLiveCollectionPersistorDelegate
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistorDelegate
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

class MakeReadOnlyFirestoreLivePersistenceObserver<SF, T>(private val field: KProperty0<T>) :
    FirestoreLivePersistenceObserver<SF, T> {

    private val persistor: FirestoreLivePersistorDelegate<T, Any?> =
        extractPersistor(field)

    override fun beforePerformingPersistence(value: T): T? =
        null //don't persist anything on value changes.

    override fun beforeValueChanges(oldValue: T, newValue: T): T? { //load the new one!
        if (newValue is Identifiable) {
            newValue.id?.let { persistor.loadAnotherDocument(it) }
        }
        return super.beforeValueChanges(oldValue, newValue)
    }
}