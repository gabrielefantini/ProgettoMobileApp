package it.polito.mad.group25.lab.fragments.review

data class Review(
    var reviewer: String?,
    var reviewed: String?,
    var tripId: String?,
    var comment: String?,
    var stars: Int?,
    var isReviewedDriver: Boolean?
)