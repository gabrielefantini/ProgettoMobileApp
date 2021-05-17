package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.google.firebase.firestore.*
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor
import it.polito.mad.group25.lab.utils.toJavaType

@Suppress("UNCHECKED_CAST")
abstract class AbstractFirestoreMultiValuePersistorDelegate<IC, T, C>(
    thisRef: C,
    id: String,
    collection: String? = null,
    targetClass: Class<T>,
    targetTypeReference: TypeReference<T>,
    dsClass: Class<IC>,
    // index for value type in collection signature. Ex we are looking for X index: in List<X> is 0, in Map<String,X> is 1
    valueTypeIndexInDSGenericParameters: Int,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?,
) : SimplePersistor<T, C>(thisRef, id, targetClass, default, observer, handler) {

    protected val store: CollectionReference

    protected val valueType: JavaType

    private val collection: String

    init {
        valueType = searchForCollectionValueType(
            targetTypeReference,
            dsClass,
            valueTypeIndexInDSGenericParameters
        )
        this.collection = collection ?: id
        Log.i(LOG_TAG, "${javaClass.simpleName} will try using ${this.collection} for $id")

        store = FirebaseFirestore.getInstance().collection(this.collection)
        store.addSnapshotListener { value, error ->
            Log.i(LOG_TAG, "Received async value for $id.")



            if (observer is FirestoreLivePersistenceObserver<*, *>) {
                (observer as FirestoreLivePersistenceObserver<QuerySnapshot, T>)
                    .onAsyncValueReceived(value, error)
                if (error != null) return@addSnapshotListener
            }

            if (value == null)
                throw IllegalArgumentException(
                    "Received value is null! " +
                            "This should never happen, check error handling of the observer!"
                )

            handler!!.notifyPersistenceLoading()

            retainOnly(value.documents.map { it.id })

            value.documentChanges.forEach {
                when (it.type) {
                    DocumentChange.Type.REMOVED -> remove(it.document.id)
                    DocumentChange.Type.ADDED -> insertElement(
                        it.document.id,
                        parseValue(it.document)
                    )
                    DocumentChange.Type.MODIFIED -> {
                        val parsed = parseValue(it.document)
                        findLocalElementWithId(it.document.id)?.let { it1 ->
                            changeElement(
                                it.document.id,
                                it1,
                                parsed
                            )
                        } ?: insertElement(it.document.id, parsed)
                    }


                }
                /*if (it.type == DocumentChange.Type.REMOVED) {
                    remove(it.document.id)
                    return@forEach
                }

                val localStored = findLocalElementWithId(it.document.id)
                val remoteStored = parseValue(it.document)
                if (localStored == null)
                    insertElement(it.document.id, remoteStored)
                else if (localStored != remoteStored) {
                    changeElement(
                        it.document.id,
                        localStored,
                        remoteStored
                    )
                }*/
            }
            handler.notifyPersistenceLoadingCompleted()
            //loadPersistenceAndSaveIt()
        }
    }


    protected abstract fun tryCreateDataStructure(clazz: Class<IC>): IC

    protected abstract fun findLocalElementWithId(id: String): Identifiable?

    protected abstract fun insertElement(id: String, element: Identifiable)

    protected abstract fun changeElement(id: String, old: Identifiable, new: Identifiable)

    protected abstract fun retainOnly(ids: List<String>)

    protected abstract fun remove(id: String)

    private fun searchForCollectionValueType(
        targetTypeReference: TypeReference<*>,
        targetCollectionClass: Class<*>,
        parameterIndex: Int
    ): JavaType {
        val type = targetTypeReference.toJavaType()
        return searchForCollectionValueTypeRecursively(type, targetCollectionClass, parameterIndex)
            ?: throw IllegalStateException("Couldn't find Identifiable collection value type starting from ${targetTypeReference.type.typeName}")
    }

    protected fun remoteInsert(identifiable: Identifiable) {
        if (identifiable.id == null) {
            //it does not exist. insert it and update the id.
            store.add(identifiable).addOnCompleteListener {
                if (it.isSuccessful)
                    identifiable.id = it.result!!.id
                else
                    observer.handleGenericException(it.exception!!)
            }
        } else {
            //it may exist, so update the old one or add it with the given id
            store.document(identifiable.id!!).get().addOnCompleteListener { task ->
                task.result!!.reference.set(identifiable).addOnCompleteListener {
                    if (!it.isSuccessful) throw RuntimeException(it.exception)
                }
            }
        }
    }

    protected fun mirrorOnRemote(objs: Set<Identifiable>) {
        val groupedIds: Map<String, Identifiable> = objs.map { it.id!! to it }.toMap()
        store.get().addOnSuccessListener { task ->
            task.documents.filter { !groupedIds.contains(it.id) }
                .forEach { it.reference.delete() }
            objs.forEach(this::remoteInsert)
        }
    }


    protected fun parseValue(ds: DocumentSnapshot): Identifiable {
        return ds.toObject(valueType.rawClass as Class<Identifiable>)!!
    }

    private fun searchForCollectionValueTypeRecursively(
        type: JavaType,
        targetCollectionClass: Class<*>,
        parameterIndex: Int
    ): JavaType? {
        if (targetCollectionClass.isAssignableFrom(type.rawClass)) {
            val valueType = type.bindings.typeParameters[parameterIndex]
            if (Identifiable::class.java.isAssignableFrom(valueType.rawClass))
                return valueType
        }
        for (parameter in type.bindings.typeParameters) {
            val computed =
                searchForCollectionValueTypeRecursively(
                    parameter,
                    targetCollectionClass,
                    parameterIndex
                )
            if (computed != null)
                return computed
        }
        return null
    }


}