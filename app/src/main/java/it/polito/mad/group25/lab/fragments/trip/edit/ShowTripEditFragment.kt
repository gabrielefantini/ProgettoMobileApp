package it.polito.mad.group25.lab.fragments.trip.edit

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.tutorial.TutorialViewModel

class ShowTripEditFragment : TripEditFragment(R.layout.trip_edit_fragment){

    private val tutorialViewModel: TutorialViewModel by activityViewModels()
    private val tutorialId = R.id.Tutorial2

    override fun onCreate(savedInstanceState: Bundle?) {

        //redirect if it's the first time visiting the fragment
        if(!tutorialViewModel.hasAlreadySeenTutorial2)
            activity?.findNavController(R.id.nav_host_fragment_content_main)
                ?.navigate(tutorialId)

        //if the previous backstack destination was a tutorial one, it will be removed
        var last = activity?.findNavController(R.id.nav_host_fragment_content_main)
            ?.previousBackStackEntry
        if(last?.destination?.id == tutorialId)
            activity?.findNavController(R.id.nav_host_fragment_content_main)?.popBackStack(tutorialId,true)

        super.onCreate(savedInstanceState)
    }
}