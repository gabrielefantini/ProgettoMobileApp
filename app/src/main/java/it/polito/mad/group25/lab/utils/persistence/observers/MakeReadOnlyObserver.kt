package it.polito.mad.group25.lab.utils.persistence.observers

import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver

class MakeReadOnlyObserver<T>(next: PersistenceObserver<T> = object : PersistenceObserver<T> {}) :
    ChainedObserver<T>(next) {

    override fun beforePerformingPersistence(value: T): T? =
        null //don't persist anything on value changes.

    override fun beforePerformingLiveValuePersistency(value: T): T? =
        null //don't persist anything on value changes.

}