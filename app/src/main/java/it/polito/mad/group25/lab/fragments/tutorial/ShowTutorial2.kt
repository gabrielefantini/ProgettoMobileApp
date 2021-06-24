package it.polito.mad.group25.lab.fragments.tutorial

import androidx.fragment.app.activityViewModels
import it.polito.mad.group25.lab.R

class ShowTutorial2: TutorialFragment(
    listOf(
        R.layout.tutorial5,
        R.layout.tutorial6
        )
){
    private val tutorialViewModel: TutorialViewModel by activityViewModels()


    override fun onDestroy() {
        super.onDestroy()
        tutorialViewModel.hasAlreadySeenTutorial2 = true
    }
}