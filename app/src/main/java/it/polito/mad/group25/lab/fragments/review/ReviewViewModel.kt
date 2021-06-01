package it.polito.mad.group25.lab.fragments.review

import android.app.Application
import it.polito.mad.group25.lab.utils.persistence.awareds.PersistenceAwareMutableLiveMap
import it.polito.mad.group25.lab.utils.persistence.awareds.persistenceAwareMutableLiveMapOf
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors
import it.polito.mad.group25.lab.utils.persistence.observers.ToastOnErrorPersistenceObserver
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel
import java.util.*

class ReviewViewModel(application: Application): PersistableViewModel(application) {
    val reviews: PersistenceAwareMutableLiveMap<String, Review>
            by Persistors.liveFirestoreMap(
                collection = "reviews",
                default = persistenceAwareMutableLiveMapOf(),
                observer = ToastOnErrorPersistenceObserver(application)
            )
    private fun generateNewId(): String {
        var id: String
        do {
            id = UUID.randomUUID().toString()
        } while (reviews.value!!.containsKey(id))
        return id
    }

    //add methods to handle reviews
    fun addReview(
        reviewer: String,
        reviewed: String,
        tripId: String,
        comment: String,
        stars: Int,
        isReviewedDriver: Boolean,
    ){
        var newReview = Review()
        newReview.id = generateNewId()
        newReview.reviewer = reviewer
        newReview.reviewed = reviewed
        newReview.tripId = tripId
        newReview.comment = comment
        newReview.stars = stars
        newReview.isReviewedDriver = isReviewedDriver
        reviews.value?.put(newReview.id!!, newReview)
    }
}