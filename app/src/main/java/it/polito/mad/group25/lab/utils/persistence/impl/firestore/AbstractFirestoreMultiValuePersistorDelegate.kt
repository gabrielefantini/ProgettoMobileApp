package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor
import it.polito.mad.group25.lab.utils.toJavaType

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

    protected var toParse: Collection<DocumentSnapshot> = mutableListOf()

    protected val NULL_VALUE = "null"

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

        getStore().get().addOnCompleteListener {
            Log.i(LOG_TAG, "Received async value for $id.")

            if(observer is FirestoreLivePersistenceObserver<*,*>){
                (observer as FirestoreLivePersistenceObserver<QuerySnapshot, T>)
                    .onAsyncValueReceived(it.result, it.exception)
            }

            if (it.result == null)
                throw IllegalArgumentException(
                    "Received value is null! " +
                            "This should never happen, check error handling of the observer!"
                )
            toParse = it.result!!.documents
            loadPersistenceAndSaveIt()
        }
    }

    protected fun getStore() = FirebaseFirestore.getInstance().collection(this.collection)

    protected abstract fun parseValues(
        clazz: Class<IC>,
        q: Collection<DocumentSnapshot>
    ): IC?

    protected abstract fun tryCreateDataStructure(clazz: Class<IC>): IC

    private fun searchForCollectionValueType(
        targetTypeReference: TypeReference<*>,
        targetCollectionClass: Class<*>,
        parameterIndex: Int
    ): JavaType {
        val type = targetTypeReference.toJavaType()
        return searchForCollectionValueTypeRecursively(type, targetCollectionClass, parameterIndex)
            ?: throw IllegalStateException("Couldn't find Identifiable collection value type starting from ${targetTypeReference.type.typeName}")
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

    protected fun insert(identifiable: Identifiable) {
        if (identifiable.id == null) {
            //it does not exist. insert it and update the id.
            getStore().add(identifiable).addOnCompleteListener {
                if (it.isSuccessful)
                    identifiable.id = it.result!!.id
                else
                    throw RuntimeException(it.exception)
            }
        } else {
            //it may exist, so update the old one or add it with the given id
            insert(identifiable.id!!, identifiable)
        }
    }

    protected fun insert(forceId: String, identifiable: Identifiable?) {
        getStore().document(forceId).addSnapshotListener { ds, error ->
            if (error != null) throw error
            ds!!.reference.set(identifiable ?: NULL_VALUE).addOnCompleteListener {
                if (it.isSuccessful)
                    identifiable?.id = forceId
                else Log.e(
                    LOG_TAG, "Error saving ${identifiable?.id} of $id on Firestore",
                    it.exception
                )
            }
        }
    }

    protected fun parseNullableValue(ds: DocumentSnapshot): Identifiable? {
        return if (ds.toString() == NULL_VALUE) null
        else ds.toObject(valueType.rawClass as Class<Identifiable>)
    }

}