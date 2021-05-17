package it.polito.mad.group25.lab.utils.persistence.observers

import android.content.Context
import android.widget.Toast
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver

class ToastOnErrorPersistenceObserver<T>(
    private val context: Context,
    next: PersistenceObserver<T> = object : PersistenceObserver<T> {}
) : ChainedObserver<T>(next) {

    private val notSavedMessage = "Couldn't save the data on the database"

    override fun afterPerformingLiveValuePersistency(value: T, ex: Exception?) {
        if (ex != null) {
            showToast(notSavedMessage, ex)
        } else
            super.afterPerformingLiveValuePersistency(value, ex)
    }

    override fun afterLoadingPersistedValue(value: T?, ex: Exception?): T? {
        return try {
            super.afterLoadingPersistedValue(value, ex)
        } catch (e: Exception) {
            showToast("Couldn't load the data from the database", e)
            null
        }
    }

    override fun afterPerformingPersistence(value: T, ex: Exception?) {
        if (ex != null) {
            showToast(notSavedMessage, ex)
        } else super.afterPerformingPersistence(value, ex)
    }

    override fun handleGenericException(ex: Exception) {
        showToast("Generic error in persistence process", ex)
    }


    private fun showToast(message: String, ex: Exception) {
        Toast.makeText(
            context,
            "$message : ${ex.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}