package it.polito.mad.group25.lab.utils.persistence.awareds

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.polito.mad.group25.lab.utils.persistence.PersistenceAware
import it.polito.mad.group25.lab.utils.persistence.PersistenceAwareWrapper
import it.polito.mad.group25.lab.utils.persistence.PersistenceContext

class PersistenceAwareLiveData<T : PersistenceAware> : LiveData<T>,
    PersistenceAwareWrapper<T> {

    constructor(value: T) : super(value)
    constructor() : super()

    override val wrapped: T
        get() = value!!

}

class PersistenceAwareMutableLiveData<T : PersistenceAware> : MutableLiveData<T>,
    PersistenceAwareWrapper<T> {

    constructor(value: T) : super(value)
    constructor() : super()

    override val wrapped: T
        get() = value!!
}

class PersistentMutableLiveData<T> : MutableLiveData<T>, PersistenceAware {
    override lateinit var persistenceContext: PersistenceContext

    constructor(value: T) : super(value)
    constructor() : super()

    override fun setValue(value: T) {
        super.setValue(value)
        if (this::persistenceContext.isInitialized)
            super.statusUpdated()
    }
}
