package it.polito.mad.group25.lab.utils.persistence.observers

import android.util.Log
import it.polito.mad.group25.lab.utils.persistence.PersistenceObserver

class TracerPersistenceObserver<T>(next: PersistenceObserver<T> = object: PersistenceObserver<T>{}) : ChainedObserver<T>(next) {

    private val LOG_TAG = "TRACER OBSERVER"

    override fun afterPerformingLiveValuePersistency(value: T, ex: Exception?) {
        Log.d(LOG_TAG, "Entering in afterPerformingLiveValuePersistency of ${javaClass.simpleName}")
        super.afterPerformingLiveValuePersistency(value, ex)
        Log.d(
            LOG_TAG,
            "Exiting from afterPerformingLiveValuePersistency of ${javaClass.simpleName}"
        )
    }

    override fun beforePerformingLiveValuePersistency(value: T): T? {
        Log.d(
            LOG_TAG,
            "Entering in beforePerformingLiveValuePersistency of ${javaClass.simpleName}"
        )
        return super.beforePerformingLiveValuePersistency(value).also {
            Log.d(
                LOG_TAG,
                "Exiting from beforePerformingLiveValuePersistency of ${javaClass.simpleName}"
            )
        }
    }

    override fun onAsyncValueReceived(value: Any?, error: Exception?) {
        Log.d(LOG_TAG, "Entering in onAsyncValueReceived of ${javaClass.simpleName}")
        super.onAsyncValueReceived(value, error)
        Log.d(LOG_TAG, "Exiting from onAsyncValueReceived of ${javaClass.simpleName}")
    }

    override fun onLiveValueChanges(newValue: T) {
        Log.d(LOG_TAG, "Entering in onLiveValueChanges of ${javaClass.simpleName}")
        super.onLiveValueChanges(newValue)
        Log.d(LOG_TAG, "ExitingFrom onLiveValueChanges of ${javaClass.simpleName}")
    }

    override fun afterLoadingPersistedValue(value: T?, ex: Exception?): T? {
        Log.d(LOG_TAG, "Entering in afterLoadingPersistedValue of ${javaClass.simpleName}")
        return super.afterLoadingPersistedValue(value, ex).also {
            Log.d(LOG_TAG, "Exiting from afterLoadingPersistedValue of ${javaClass.simpleName}")
        }
    }

    override fun afterPerformingPersistence(value: T, ex: Exception?) {
        Log.d(LOG_TAG, "Entering in afterPerformingPersistence of ${javaClass.simpleName}")
        super.afterPerformingPersistence(value, ex)
        Log.d(LOG_TAG, "Exiting from afterPerformingPersistence of ${javaClass.simpleName}")
    }

    override fun afterValueChanges(value: T) {
        Log.d(LOG_TAG, "Entering in afterValueChanges of ${javaClass.simpleName}")
        super.afterValueChanges(value)
        Log.d(LOG_TAG, "Exiting from afterValueChanges of ${javaClass.simpleName}")
    }

    override fun beforeLoadingPersistedValue(): Boolean {
        Log.d(LOG_TAG, "Entering in beforeLoadingPersistedValue of ${javaClass.simpleName}")
        return super.beforeLoadingPersistedValue().also {
            Log.d(LOG_TAG, "Exiting from beforeLoadingPersistedValue of ${javaClass.simpleName}")
        }
    }

    override fun beforePerformingPersistence(value: T): T? {
        Log.d(LOG_TAG, "Entering in beforePerformingPersistence of ${javaClass.simpleName}")
        return super.beforePerformingPersistence(value).also {
            Log.d(LOG_TAG, "Exiting from beforePerformingPersistence of ${javaClass.simpleName}")
        }
    }

    override fun beforeValueChanges(oldValue: T, newValue: T): T? {
        Log.d(LOG_TAG, "Entering in beforeValueChanges of ${javaClass.simpleName}")
        return super.beforeValueChanges(oldValue, newValue).also {
            Log.d(LOG_TAG, "Exiting from beforeValueChanges of ${javaClass.simpleName}")
        }
    }

}