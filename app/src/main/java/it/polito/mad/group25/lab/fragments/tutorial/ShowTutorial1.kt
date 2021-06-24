package it.polito.mad.group25.lab.fragments.tutorial

import androidx.fragment.app.activityViewModels
import it.polito.mad.group25.lab.R

class ShowTutorial1: TutorialFragment(
    listOf(
        R.layout.tutorial1,
        R.layout.tutorial2,
        R.layout.tutorial3,
        R.layout.tutorial4
    ),
){
    private val tutorialViewModel: TutorialViewModel by activityViewModels()


    override fun onDestroy() {
        super.onDestroy()
        tutorialViewModel.hasAlreadySeenTutorial1 = true
    }
}