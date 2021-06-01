package it.polito.mad.group25.lab.fragments.review

import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware
import java.io.Serializable

class Review :  AbstractPersistenceAware(), Identifiable, Serializable {
    override var id: String? by onChangeUpdateStatus(null)
    var reviewer: String? by onChangeUpdateStatus(null)
    var reviewed: String? by onChangeUpdateStatus(null)
    var tripId: String? by onChangeUpdateStatus(null)
    var comment: String? by onChangeUpdateStatus(null)
    var stars: Int? by onChangeUpdateStatus(null)
    var isReviewedDriver: Boolean? by onChangeUpdateStatus(null)
}