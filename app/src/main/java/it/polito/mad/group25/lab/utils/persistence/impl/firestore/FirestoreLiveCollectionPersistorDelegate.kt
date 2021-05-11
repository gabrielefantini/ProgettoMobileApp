package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler

class FirestoreLiveCollectionPersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    targetClass: Class<T>,
    targetTypeReference: TypeReference<T>,
    default: T,
    observer: FirestoreLivePersistenceObserver<QuerySnapshot, T>,
    handler: AbstractPersistenceHandler<T, *>?
) : AbstractFirestoreMultiValuePersistorDelegate<MutableCollection<Identifiable?>, T, C>(
    thisRef, id, collection, targetClass, targetTypeReference,
    MutableCollection::class.java as Class<MutableCollection<Identifiable?>>,
    0, default, observer, handler
) {


    init {
        initialized()
    }

    override fun parseValues(
        clazz: Class<MutableCollection<Identifiable?>>,
        q: Collection<DocumentSnapshot>
    ): MutableCollection<Identifiable?>? {
        Log.i(LOG_TAG, "Parsing values loaded from firestore for $id")
        return if (q.isEmpty()) null else
            q.map(this::parseNullableValue).toCollection(tryCreateDataStructure(clazz))
    }

    override fun tryCreateDataStructure(clazz: Class<MutableCollection<Identifiable?>>): MutableCollection<Identifiable?> =
        clazz.constructors[0].newInstance() as MutableCollection<Identifiable?>


    override fun <R> doPersist(value: R) {
        Log.i(LOG_TAG, "Persisting values for $id on firestore")
        value as Collection<Identifiable?> //if cast fails just throw up
        value.filterNotNull().forEach { super.insert(it) }
    }

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? =
        parseValues(targetClass as Class<MutableCollection<Identifiable?>>, toParse) as R?

}