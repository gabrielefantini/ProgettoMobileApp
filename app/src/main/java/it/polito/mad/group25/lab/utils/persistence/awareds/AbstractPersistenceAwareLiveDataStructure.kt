package it.polito.mad.group25.lab.utils.persistence.awareds

import androidx.lifecycle.MutableLiveData
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware
import it.polito.mad.group25.lab.utils.persistence.PersistenceAware

@Suppress("UNCHECKED_CAST")
abstract class AbstractPersistenceAwareLiveDataStructure<ME : AbstractPersistenceAwareLiveDataStructure<ME>>
    (innerPADS: AbstractPersistenceAware) : MutableLiveData<ME>(), PersistenceAware by innerPADS {

    init {
        this.value = this as ME
        innerPADS.onStatusUpdateListeners.add { this.value = this }
    }

}