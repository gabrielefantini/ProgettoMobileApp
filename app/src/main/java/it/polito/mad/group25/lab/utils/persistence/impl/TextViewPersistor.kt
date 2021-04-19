package it.polito.mad.group25.lab.utils.persistence.impl

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import it.polito.mad.group25.lab.utils.persistence.ViewPersistor

abstract class TextViewPersistor<V : TextView, T>(
    val loader: (T, String) -> String?,
    val saver: (T, String, String) -> Unit
) : ViewPersistor<V, T> {

    override fun load(id: String, view: V, storage: T): Boolean {
        val toSet = loader(storage, id)
        toSet?.let { view.text = it }
        return toSet != null
    }

    override fun save(id: String, view: V, target: T) {
        saver(target, id, view.text.toString())
    }
}

class TextViewBundlePersistor<T : TextView> :
    TextViewPersistor<T, Bundle>(
        { storage, id -> storage.getString(id) },
        { storage, id, value -> storage.putString(id, value) })

class TextViewIntentPersistor<T : TextView> :
    TextViewPersistor<T, Intent>(Intent::getStringExtra, Intent::putExtra)

/**
 * As said on the slack channel "group25", unique json object serialization was not mandatory
 * An example of JSONObject serialization is given but commented
 */
class TextViewSharedPreferencesPersistor<T : TextView> :
    TextViewPersistor<T, SharedPreferences>(
        { storage, id ->
            /*val json = JSONObject(storage.getString(id, "{}"))
            json.getString(id)*/
            storage.getString(id, null)
        },
        { storage, id, value ->
            with(storage.edit()) {
                /*val json = JSONObject()
                json.put(id,value)
                putString(json.toString())*/
                putString(id, value);
                apply()
            }
        }
    )
