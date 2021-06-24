package it.polito.mad.group25.lab.fragments.tutorial

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import it.polito.mad.group25.lab.R

class ShowTutorial2: TutorialFragment(
    listOf(
        R.layout.tutorial1
    )
){
    private val tutorialViewModel: TutorialViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tutorialViewModel.hasAlreadySeenTutorial2 = true
    }
}