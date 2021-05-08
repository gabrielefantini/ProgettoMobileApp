package it.polito.mad.group25.lab.utils.persistence.impl

import androidx.lifecycle.LiveData
import it.polito.mad.group25.lab.utils.BidirectionalMapper
import it.polito.mad.group25.lab.utils.persistence.DelegatingPersistencyObserver
import it.polito.mad.group25.lab.utils.persistence.DelegatingPersistor
import it.polito.mad.group25.lab.utils.persistence.SimplePersistor
import kotlin.reflect.KProperty

class LiveDataMapper<Q> : BidirectionalMapper<LiveData<Q>, Q> {
    override fun directMap(a: LiveData<Q>): Q? = a.value
    override fun reverseMap(b: Q): LiveData<Q> = object : LiveData<Q>(b) {}
}

/**
 * A Persistor Wrapper which purpose is only to subscribe the wrapped persistor to the value changes.
 */
class LiveDataPersistorSubscriber<Q, C>(
    wrapped: SimplePersistor<Q, C>,
    default: LiveData<Q>,
    property: KProperty<*>
) : DelegatingPersistor<LiveData<Q>, Q, C>(
    wrapped,
    LiveDataMapper(),
    default
) {

    init {
        this.observer =
            object : DelegatingPersistencyObserver<LiveData<Q>, Q>(
                wrapped.observer,
                LiveDataMapper()
            ) {
                // qui e non nell'after per capire se il LiveData è davvero cambiato e non fare sottoscrizioni doppie!
                override fun beforeValueChanges(
                    oldValue: LiveData<Q>,
                    newValue: LiveData<Q>
                ): LiveData<Q>? {
                    return super.beforeValueChanges(oldValue, newValue)?.apply {
                        if (this !== oldValue)
                            observeForever { v -> wrapped.setValue(thisRef, property, v) }
                    }
                }
            }
    }


}


