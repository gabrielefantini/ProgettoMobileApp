package it.polito.mad.group25.lab.fragments.trip.list.impl

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.fragments.trip.list.GenericTripListFragment
import it.polito.mad.group25.lab.fragments.tutorial.TutorialViewModel


class OthersTripListFragment : GenericTripListFragment(false) {

    private val tutorialViewModel: TutorialViewModel by activityViewModels()
    private val tutorialId = R.id.Tutorial

    override fun onCreate(savedInstanceState: Bundle?) {

        //redirect if it's the first time visiting the fragment
        if(!tutorialViewModel.hasAlreadySeenTutorial1)
            activity?.findNavController(R.id.nav_host_fragment_content_main)
                ?.navigate(tutorialId)

        //if the previous backstack destination was a tutorial one, it will be removed
        var last = activity?.findNavController(R.id.nav_host_fragment_content_main)
            ?.previousBackStackEntry
        if(last?.destination?.id == tutorialId)
            activity?.findNavController(R.id.nav_host_fragment_content_main)?.popBackStack(tutorialId,true)

        super.onCreate(savedInstanceState)
    }

    override fun filterTrip(trip: Trip): Boolean = trip.ownerId != authenticationContext.userId() &&
            trip.tripStartDate > System.currentTimeMillis() + 60 * 1000

    override fun boughtTrip(): Boolean {
        return false
    }

}