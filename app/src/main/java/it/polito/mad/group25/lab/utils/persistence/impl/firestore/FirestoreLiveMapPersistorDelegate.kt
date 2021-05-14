package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver

@Suppress("UNCHECKED_CAST")
class FirestoreLiveMapPersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    private val collection: String? = null,
    targetClass: Class<T>,
    targetTypeReference: TypeReference<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?,
) : AbstractFirestoreMultiValuePersistorDelegate<MutableMap<String, Identifiable>, T, C>(
    thisRef, id, collection, targetClass, targetTypeReference,
    MutableMap::class.java as Class<MutableMap<String, Identifiable>>,
    1, default, observer, handler
) {


    private lateinit var map: MutableMap<String, Identifiable>

    init {
        initialized()
    }

    override fun <R> doLoadPersistence(targetClass: Class<R>): R? {
        map =
            if (value is MutableMap<*, *>) value as MutableMap<String, Identifiable>
            else tryCreateDataStructure(targetClass as Class<MutableMap<String, Identifiable>>)
        return map as R
    }


    override fun tryCreateDataStructure(clazz: Class<MutableMap<String, Identifiable>>) =
        clazz.constructors[0].newInstance() as MutableMap<String, Identifiable>


    override fun <R> doPersist(value: R) {
        Log.i(LOG_TAG, "Persisting values for $id on firestore")
        value as Map<String, Identifiable> // if cast fails just throw up
        mirrorOnRemote(value.values.toSet())
    }

    override fun findLocalElementWithId(id: String): Identifiable? = map[id]

    override fun insertElement(id: String, element: Identifiable) {
        map[id] = element
    }

    override fun changeElement(id: String, old: Identifiable, new: Identifiable) {
        map[id] = new
    }

    override fun retainOnly(ids: List<String>) {
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            if (!ids.contains(iterator.next().key))
                iterator.remove()
        }
    }

    override fun remove(id: String) {
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            if (id == iterator.next().key)
                iterator.remove()
        }
    }


}