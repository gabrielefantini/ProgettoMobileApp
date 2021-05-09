package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import android.util.Log
import com.google.firebase.firestore.*
import it.polito.mad.group25.lab.AuthenticationContext
import it.polito.mad.group25.lab.utils.extractGenericType
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceHandler
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor

abstract class AbstractFirestorePersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    val collection: String? = null,
    val document: String? = null,
    targetClass: Class<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?
) : SimplePersistor<T, C>(thisRef, id, targetClass, default, observer, handler) {


    protected val NULL_VALUE = "null"

    // Prendo la collection data oppure il l'id del field in questione
    // Prendo il documento dato oppure quello che appartiene all'utente selezionato.
    // Esempio pratico per gli utenti: Collection di utenti in cui l'oggetto che mi interessa è quello dell'utente x.
    var store: DocumentReference

    init {
        val collection = collection ?: id
        val document = document ?: AuthenticationContext.userID
        ?: throw IllegalStateException(
            "Nor document id was provided neither user id is set in the AuthenticationContext." +
                    "${javaClass.simpleName} can't load the required data of ${id}."
        )
        Log.i(LOG_TAG, "${javaClass.simpleName} will try using $collection/$document for $id")
        store = FirebaseFirestore.getInstance()
            .collection(collection)
            .document(document)
    }

    // perchè i figli possano riutilizzare questa strategia senza avere il binding sul tipo del padre.
    protected fun <Q> parseNullableValue(clazz: Class<Q>, doc: DocumentSnapshot?): Q? {
        Log.i(LOG_TAG, "Parsing value loaded from firestore for $id")
        return doc?.let {
            if (it.toString() != NULL_VALUE) it.toObject(clazz) as Q else null
        }
    }

    // perchè i figli possano riutilizzare questa strategia senza avere il binding sul tipo del padre.
    protected fun <Q> doPersistNullableValue(value: Q) {
        Log.i(LOG_TAG, "Persisting value for $id on firestore")
        store.set(value ?: NULL_VALUE)
    }

}


abstract class AbstractFirestoreCollectionPersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    val collection: String? = null,
    targetClass: Class<T>,
    default: T,
    observer: PersistenceObserver<T>,
    handler: AbstractPersistenceHandler<T, *>?,
) : SimplePersistor<T, C>(thisRef, id, targetClass, default, observer, handler) {

    var store: CollectionReference

    init {
        val collection = collection ?: id
        Log.i(LOG_TAG, "${javaClass.simpleName} will try using $collection for $id")
        store = FirebaseFirestore.getInstance().collection(collection)
    }

    // perchè i figli possano riutilizzare questa strategia senza avere il binding sul tipo del padre.
    protected fun <A : MutableCollection<Any?>> parseValues(
        clazz: Class<A>,
        q: QuerySnapshot?
    ): A? {
        Log.i(LOG_TAG, "Parsing values loaded from firestore for $id")
        val innerType: Class<Any?> = targetClass.extractGenericType() as Class<Any?>
        return q?.documents?.mapNotNull { d -> d.toObject(innerType) }
            ?.toCollection(tryCreateCollection(clazz, innerType))
    }

    private fun <I, C : MutableCollection<I>>
            tryCreateCollection(clazz: Class<C>, innerType: Class<I>): C =
        clazz.constructors[0].newInstance() as C


    // perchè i figli possano riutilizzare questa strategia senza avere il binding sul tipo del padre.
    protected fun <E, A : Collection<E>> doPersistValues(values: A) {
        Log.i(LOG_TAG, "Persisting values for $id on firestore")
        values.filterNotNull().forEach { store.add(it) }
    }

}
