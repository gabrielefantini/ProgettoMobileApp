package it.polito.mad.group25.lab.fragments.review

import android.app.Application
import it.polito.mad.group25.lab.utils.persistence.awareds.PersistenceAwareMutableLiveMap
import it.polito.mad.group25.lab.utils.persistence.awareds.persistenceAwareMutableLiveMapOf
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors
import it.polito.mad.group25.lab.utils.persistence.observers.ToastOnErrorPersistenceObserver
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel

class ReviewViewModel(application: Application): PersistableViewModel(application) {
    val reviews: PersistenceAwareMutableLiveMap<String, Review>
            by Persistors.liveFirestoreMap(
                collection = "reviews",
                default = persistenceAwareMutableLiveMapOf(),
                observer = ToastOnErrorPersistenceObserver(application)
            )
    //add methods to handle reviews

}