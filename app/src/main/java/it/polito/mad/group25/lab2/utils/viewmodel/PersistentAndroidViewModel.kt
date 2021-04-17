package it.polito.mad.group25.lab2.utils.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import it.polito.mad.group25.lab2.utils.distanceFrom
import it.polito.mad.group25.lab2.utils.persistence.DifferentiatedSerdeStrategy


/**
 * View model which automatically performs persistence through reflection
 */
abstract class PersistentAndroidViewModel(application: Application) :
    AndroidViewModel(application) {

    init {
        loadPersistence()
    }

    override fun onCleared() {
        super.onCleared()
        this.persist()
    }

    /**
     * Function which provides custom serialization and deserialization strategies.
     * See object doc to more info
     **/
    protected open fun customSerdeStrategies(): List<DifferentiatedSerdeStrategy<*, SharedPreferences, SharedPreferences.Editor>> =
        listOf()

    private val customStrategies = this.customSerdeStrategies()
    private val customStrategiesMapped =
        customStrategies.map { it.deserializedType().java to it }.toMap()


    private fun provideCustomStrategyForType(typo: Class<*>): DifferentiatedSerdeStrategy<Any, SharedPreferences, SharedPreferences.Editor>? {
        return (customStrategiesMapped[typo]
            ?: customStrategies.filter { it.canHandle(typo) && it.canProvide(typo) }
                .minByOrNull {
                    typo.distanceFrom(it.deserializedType().java)!! + typo.distanceFrom(it.serializedType().java)!!
                }) as? DifferentiatedSerdeStrategy<Any, SharedPreferences, SharedPreferences.Editor>
    }

    private fun persist() {
        val storage = getStorage()
        val storageEditor = storage.edit()

        javaClass.declaredFields.forEach { toPersistField ->
            val id = "${javaClass.canonicalName}.${toPersistField.name}"
            val toPersist = toPersistField.apply { isAccessible = true }.get(this)
            toPersist?.let {
                val customStrategy = provideCustomStrategyForType(toPersistField.type)
                if (customStrategy != null)
                    customStrategy(it, id, storageEditor)
                else storageEditor.putString(id, it.toString())
            }
        }
        storageEditor.apply()
    }

    private fun loadPersistence() {
        val storage = getStorage()
        javaClass.declaredFields.forEach { toPersistField ->
            toPersistField.apply { isAccessible = true }
            val id = "${javaClass.canonicalName}.${toPersistField.name}"
            val customStrategy = provideCustomStrategyForType(toPersistField.type)
            val persistedValue = if (customStrategy != null)
                customStrategy(id, storage)
            else storage.getString(id, null)
            toPersistField.set(this, persistedValue)
        }
    }

    private fun getStorage() = getApplication<Application>()
        .getSharedPreferences(javaClass.canonicalName, Context.MODE_PRIVATE)


}