package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group25.lab.AuthenticationContext
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor
import kotlin.reflect.KProperty


open class FirestorePersistorDelegate<T, C>(
    thisRef: C,
    property: KProperty<*>,
    protected val collection: String? = null,
    protected val document: String? = null,
    default: T,
    onValueChanges: (T) -> Unit = {},
    onLoadedPersistedValue: (T) -> Unit = {},
) : SimplePersistor<T, C>(
    thisRef,
    property,
    default,
    onValueChanges,
    onLoadedPersistedValue
) {


    protected val store = FirebaseFirestore.getInstance()
        .collection(collection ?: id)
        .document(
            document ?: AuthenticationContext.userID
            ?: throw IllegalStateException(
                "Nor document id was provided neither user id is set in the AuthenticationContext." +
                        "${javaClass.simpleName} can't load the required data of ${property.name}."
            )
        )

    override fun persist(value: T) {
        store.set(value ?: "null")
    }

    override fun loadPersistence(): T? {
        return store.get().result?.toObject(targetClass) as T
    }

}
