package it.polito.mad.group25.lab.utils.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import it.polito.mad.group25.lab.utils.persistence.PersistableContainer

abstract class PersistableViewModel(application: Application) : AndroidViewModel(application),
    PersistableContainer {
    override fun getContext(): Context = getApplication()
}