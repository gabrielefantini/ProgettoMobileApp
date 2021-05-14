package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver

@Suppress("UNCHECKED_CAST")
class FirestoreLiveCollectionPersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    targetClass: Class<T>,
    targetTypeReference: TypeReference<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?
) : AbstractFirestoreMultiValuePersistorDelegate<MutableCollection<Identifiable>, T, C>(
    thisRef, id, collection, targetClass, targetTypeReference,
    MutableCollection::class.java as Class<MutableCollection<Identifiable>>,
    0, default, observer, handler
) {

    private lateinit var collection: MutableCollection<Identifiable>

    init {
        initialized()
    }


    override fun <R> doPersist(value: R) {
        Log.i(LOG_TAG, "Persisting values for $id on firestore")
        value as Collection<Identifiable> //if cast fails just throw up
        mirrorOnRemote(value.toSet())
    }

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? {
        collection =
            if (value is MutableCollection<*>) value as MutableCollection<Identifiable>
            else tryCreateDataStructure(targetClass as Class<MutableCollection<Identifiable>>)
        return collection as R
    }


    override fun findLocalElementWithId(id: String): Identifiable? = collection.find { it.id == id }


    override fun insertElement(id: String, element: Identifiable) {
        collection.add(element)
    }

    override fun changeElement(id: String, old: Identifiable, new: Identifiable) {
        collection.remove(old)
        collection.add(new)
    }

    override fun retainOnly(ids: List<String>) {
        collection.retainAll { ids.contains(it.id) }
    }

    override fun tryCreateDataStructure(clazz: Class<MutableCollection<Identifiable>>): MutableCollection<Identifiable> =
        clazz.constructors[0].newInstance() as MutableCollection<Identifiable>

    override fun remove(id: String) {
        val iterator = collection.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().id == id)
                iterator.remove()
        }
    }

}