package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import it.polito.mad.group25.lab.utils.persistence.PersistencyObserver

class FirestorePersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    document: String? = null,
    default: T,
    protected val targetClass: Class<T>,
    observer: PersistencyObserver<T> = object : PersistencyObserver<T> {}
) : AbstractFirestorePersistorDelegate<T, C>(thisRef, id, collection, document, default, observer) {

    override fun doPersist(value: T) = doPersistNullableValue(value)

    override fun doLoadPersistence(): T? = doLoadNullableValue(targetClass, store.get().result)

}

class FirestoreCollectionPersistorDelegate<Q, T : MutableCollection<Q>, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    default: T,
    protected val collectionCreator: () -> T = { default::class.java.constructors[0].newInstance() as T },
    protected val innerClass: Class<Q>,
    observer: PersistencyObserver<T> = object : PersistencyObserver<T> {}
) : AbstractFirestoreCollectionPersistorDelegate<Q, T, C>(
    thisRef,
    id,
    collection,
    default,
    observer
) {

    override fun doPersist(value: T) = doPersistValues(value)

    override fun doLoadPersistence(): T? =
        doLoadValues(collectionCreator, innerClass, store.get().result)


}
