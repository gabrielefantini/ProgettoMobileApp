package it.polito.mad.group25.lab.utils.persistence.observers

import it.polito.mad.group25.lab.utils.persistence.LiveDataPersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistenceObserver

class MakeReadOnlyObserver<SF, T> : PersistenceObserver<T>, LiveDataPersistenceObserver<T>,
    FirestoreLivePersistenceObserver<SF, T> {

    override fun beforePerformingPersistence(value: T): T? =
        null //don't persist anything on value changes.

    override fun beforePerformingLiveValuePersistency(value: T): T? =
        null //don't persist anything on value changes.

}