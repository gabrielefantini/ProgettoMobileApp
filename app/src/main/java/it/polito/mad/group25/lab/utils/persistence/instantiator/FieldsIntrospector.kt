package it.polito.mad.group25.lab.utils.persistence.instantiator

import androidx.lifecycle.LiveData
import it.polito.mad.group25.lab.utils.getAllFields
import it.polito.mad.group25.lab.utils.isStatic
import it.polito.mad.group25.lab.utils.persistence.*
import java.lang.reflect.Field

//Subscribes to all LiveData of the object and injects all persistence context!
private object FieldsIntrospector {
    fun <T, C> introspect(
        value: Any?,
        persistor: SimplePersistor<T, C>,
        valueField: Field? = null,
        alreadySeen: MutableSet<Field> = mutableSetOf()
    ) {
        if (value == null) return

        if (valueField != null && alreadySeen.contains(valueField)) return

        if (LiveData::class.java.isAssignableFrom(value::class.java)) {
            value as LiveData<Any?>
            introspect(value.value, persistor)
            value.observeForever {
                introspect(it, persistor)
                persistor.persist()
            }
        }
        if (value is PersistenceAware) {
            value.persistenceContext = PersistenceContext { persistor.doPersist(it) }
        }
        for (field in value::class.java.getAllFields().filter { !it.isStatic() }) {
            field.isAccessible = true
            introspect(
                field.get(value),
                persistor,
                field,
                alreadySeen = alreadySeen.apply { add(field) })
        }
    }
}

class FieldsIntrospectorObserver<T, C>(
    baseObserver: PersistenceObserver<T>,
    private val persistor: SimplePersistor<T, C>
) : PersistenceObserver<T> by baseObserver, LiveDataPersistenceObserver<Any?> {

    override fun beforeValueChanges(oldValue: T, newValue: T): T? {
        val supComp = super.beforeValueChanges(oldValue, newValue)
        return if (supComp === oldValue) supComp
        else supComp.apply { FieldsIntrospector.introspect(supComp, persistor) }
    }

    override fun onLiveValueChanges(newValue: Any?) {
        FieldsIntrospector.introspect(newValue, persistor)
    }

}

