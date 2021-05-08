package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.group25.lab.AuthenticationContext
import it.polito.mad.group25.lab.utils.persistence.PersistencyObserver
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor

abstract class AbstractFirestorePersistorDelegate<T, C>(
    thisRef: C,
    id: String,
    val collection: String? = null,
    val document: String? = null,
    default: T,
    observer: PersistencyObserver<T> = object : PersistencyObserver<T> {}
) : SimplePersistor<T, C>(thisRef, id, default, observer) {


    protected val NULL_VALUE = "null"

    // Prendo la collection data oppure il l'id del field in questione
    // Prendo il documento dato oppure quello che appartiene all'utente selezionato.
    // Esempio pratico per gli utenti: Collection di utenti in cui l'oggetto che mi interessa è quello dell'utente x.
    var store = FirebaseFirestore.getInstance()
        .collection(collection ?: id)
        .document(
            document ?: AuthenticationContext.userID
            ?: throw IllegalStateException(
                "Nor document id was provided neither user id is set in the AuthenticationContext." +
                        "${javaClass.simpleName} can't load the required data of ${id}."
            )
        )

    // perchè i figli possano riutilizzare questa strategia senza avere il binding sul tipo del padre.
    protected fun <Q> doLoadNullableValue(clazz: Class<Q>, doc: DocumentSnapshot?): Q? {
        return doc?.let {
            if (it.toString() != NULL_VALUE) it.toObject(clazz) as Q else null
        }
    }

    // perchè i figli possano riutilizzare questa strategia senza avere il binding sul tipo del padre.
    protected fun <Q> doPersistNullableValue(value: Q) {
        store.set(value ?: NULL_VALUE)
    }

}


abstract class AbstractFirestoreCollectionPersistorDelegate<Q, T : MutableCollection<Q>, C>(
    thisRef: C,
    id: String,
    val collection: String? = null,
    default: T,
    observer: PersistencyObserver<T> = object : PersistencyObserver<T> {}
) : SimplePersistor<T, C>(thisRef, id, default, observer) {

    var store = FirebaseFirestore.getInstance()
        .collection(collection ?: id)

    // perchè i figli possano riutilizzare questa strategia senza avere il binding sul tipo del padre.
    protected fun <E, A : MutableCollection<E>> doLoadValues(
        collectionCreator: () -> A,
        clazz: Class<E>,
        q: QuerySnapshot?
    ): A? = q?.documents?.mapNotNull { d -> d.toObject(clazz) }?.toCollection(collectionCreator())


    // perchè i figli possano riutilizzare questa strategia senza avere il binding sul tipo del padre.
    protected fun <E, A : MutableCollection<E>> doPersistValues(values: A) {
        values.filterNotNull().forEach { store.add(it) }
    }

}
