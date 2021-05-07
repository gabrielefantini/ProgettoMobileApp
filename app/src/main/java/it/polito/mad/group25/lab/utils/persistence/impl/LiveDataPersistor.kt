package it.polito.mad.group25.lab.utils.persistence.impl

import androidx.lifecycle.MutableLiveData
import it.polito.mad.group25.lab.utils.persistence.PersistencyObserver
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor
import kotlin.reflect.KProperty


abstract class LivePersistencyObserver<Q, T : MutableLiveData<Q>> :
    PersistencyObserver<T>() {

    /**
     * Intercepts the instant before the live data assignment. Has to return the value to be assigned.
     */
    open fun beforeLiveDataUpdate(oldValue: Q?, newValue: Q): Q? = newValue

    /**
     * Intercepts the instant after the live data assignment.
     */
    open fun afterLiveDataUpdate(value: Q?) {}

}

/**
 * A Persistor Wrapper which purpose is only to subscribe the wrapped persistor to the value changes.
 */
class LiveDataPersistorSubscriber<Q, T : MutableLiveData<Q>, C>(
    thisRef: C,
    property: KProperty<*>,
    private val wrapped: SimplePersistor<Q, C>,
    default: T,
    observer: LivePersistencyObserver<Q, T> = object : LivePersistencyObserver<Q, T>() {}
) : SimplePersistor<T, C>(thisRef, property, default, observer) {

    init {
        this.observer = object : PersistencyObserver<T>() { //TODO INGLOBA L'ALTRO
            override fun afterValueChanges(value: T) {
                super.afterValueChanges(value)
                value.observeForever { v -> wrapped.persist(v) }
            }
        }
    }

    override fun doLoadPersistence(): T = MutableLiveData(wrapped.doLoadPersistence()) as T

    override fun doPersist(value: T) {
        value.value?.let { wrapped.doPersist(it) }
    }

}
