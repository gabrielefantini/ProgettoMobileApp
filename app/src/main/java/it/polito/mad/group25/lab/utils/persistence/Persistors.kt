package it.polito.mad.group25.lab.utils.persistence

import com.fasterxml.jackson.core.type.TypeReference
import it.polito.mad.group25.lab.utils.persistence.impl.SharedPreferencesPersistorDelegate
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KClass

object Persistors {


    private fun <T, C> instantiateProvider(
        clazz: KClass<Persistor<T, C>>,
        vararg args: Any?
    ): PropertyDelegateProvider<C, Persistor<T, C>> {
        return PropertyDelegateProvider { thisRef, property ->
            clazz.constructors.iterator().next().call(thisRef, property, *args)
        }
    }


    fun <T> sharedPreferences(
        default: T,
        typeReference: TypeReference<T>? = null,
        onValueChanges: (T) -> Unit = {},
        onLoadedPersistedValue: (T) -> Unit = {}
    ): PropertyDelegateProvider<PersistableContainer, Persistor<T, PersistableContainer>> =
        instantiateProvider(
            SharedPreferencesPersistorDelegate::class as KClass<Persistor<T, PersistableContainer>>,
            default,
            typeReference,
            onValueChanges,
            onLoadedPersistedValue
        )
}

