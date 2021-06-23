package it.polito.mad.group25.lab.fragments.tutorial

import android.app.Application
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel

class TutorialViewModel(application: Application): PersistableViewModel(application) {
    var hasAlreadySeenTutorial1: Boolean by Persistors.sharedPreferences(default = false)
    var hasAlreadySeenTutorial2: Boolean by Persistors.sharedPreferences(default = false)
}