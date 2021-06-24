package it.polito.mad.group25.lab.fragments.tutorial

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import it.polito.mad.group25.lab.R

class ShowTutorial1: TutorialFragment(
    listOf(
        R.layout.tutorial1,
        R.layout.tutorial2,
        R.layout.tutorial3
    ),
){
    private val tutorialViewModel: TutorialViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tutorialViewModel.hasAlreadySeenTutorial1 = true
    }
}