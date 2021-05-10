package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.genericType
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler

class FirestoreLiveCollectionPersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    targetClass: Class<T>,
    default: T,
    observer: FirestoreLivePersistenceObserver<QuerySnapshot, T>,
    handler: AbstractPersistenceHandler<T, *>?
) : AbstractFirestoreMultiValuePersistorDelegate<T, C>(
    thisRef, id, collection, targetClass, default, observer, handler
) {

    private fun <R : MutableCollection<Any?>> parseValues(
        clazz: Class<R>,
        q: Collection<DocumentSnapshot>
    ): R? {
        Log.i(LOG_TAG, "Parsing values loaded from firestore for $id")
        val innerType: Class<Any?> = targetClass.genericType()[0] as Class<Any?>
        return q.map { d -> d.toObject(innerType) }.toCollection(tryCreateCollection(clazz))
            .let { if (it.isEmpty()) null else it }
    }

    private fun <I, C : MutableCollection<I>>
            tryCreateCollection(clazz: Class<C>): C =
        clazz.constructors[0].newInstance() as C


    override fun <R> doPersist(value: R) {
        Log.i(LOG_TAG, "Persisting values for $id on firestore")
        value as Collection<Any?>
        value.filterNotNull().forEach { store.add(it).apply(this::handleInsertion) }
    }

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? =
        parseValues(targetClass as Class<MutableCollection<Any?>>, toParse) as R?

}