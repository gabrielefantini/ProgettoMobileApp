package it.polito.mad.group25.lab2.utils.persistence.impl

import android.content.SharedPreferences
import it.polito.mad.group25.lab2.utils.persistence.DifferentiatedSerdeStrategy
import java.io.File
import kotlin.reflect.KClass

class FileSharedPreferencesSerde(private val default: () -> File?) :
    DifferentiatedSerdeStrategy<File, SharedPreferences, SharedPreferences.Editor> {

    constructor(path: String) : this({ File(path) })
    constructor(parent: File, path: String) : this({ File(parent, path) })
    constructor() : this({ null })

    override fun deserialize(id: String, storage: SharedPreferences): File? =
        storage.getString(id, null)?.let { File(it) } ?: default()

    override fun serialize(instance: File, id: String, storage: SharedPreferences.Editor) {
        storage.putString(id, instance.path)
    }

    override fun deserializedType(): KClass<File> = File::class

    override fun serializedType(): KClass<File> = File::class

}