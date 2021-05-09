package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver

class FirestorePersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    document: String? = null,
    targetClass: Class<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?
) : AbstractFirestorePersistorDelegate<T, C>(
    thisRef, id, collection, document,
    targetClass, default, observer, handler
) {


    override fun <R> doLoadPersistence(targetClass: Class<R>): R? =
        parseNullableValue(targetClass, store.get().result)

    override fun <R> doPersist(value: R) = doPersistNullableValue(value)

}

class FirestoreCollectionPersistorDelegate<Q, T : MutableCollection<Q>, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    targetClass: Class<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?
) : AbstractFirestoreCollectionPersistorDelegate<T, C>(
    thisRef, id, collection, targetClass,
    default, observer, handler
) {

    override fun <R> doPersist(value: R) = doPersistValues(value as Collection<*>)

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? =
        parseValues(targetClass as Class<MutableCollection<Any?>>, store.get().result) as R


}
