package it.polito.mad.group25.lab.fragments.trip.filter

import androidx.lifecycle.ViewModel

class TripFilterViewModel: ViewModel() {
    private var filter: MutableMap<String, String> = mutableMapOf()

    fun getFilter(): MutableMap<String, String> {
        return filter
    }
    fun flushFilter(){
        filter.clear()
    }

}
