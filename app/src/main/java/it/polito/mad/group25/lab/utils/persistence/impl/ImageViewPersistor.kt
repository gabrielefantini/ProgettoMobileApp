package it.polito.mad.group25.lab.utils.persistence.impl

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import it.polito.mad.group25.lab.utils.persistence.ViewPersistor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream


abstract class ImageViewPersistor<T>(
    val saver: (String, T, File) -> Unit,
    private val parent: File,
) : ViewPersistor<ImageView, T> {

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    override fun load(id: String, view: ImageView, storage: T): Boolean {
        val file = File(parent, id)
        return if (file.exists()) {
            val bytes = FileInputStream(file).readBytes()
            view.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            true
        } else false
    }

    override fun save(id: String, view: ImageView, target: T) {
        val drawable = view.drawable
        val bitmap: Bitmap = if (drawable is BitmapDrawable)
            drawable.bitmap else getBitmapFromDrawable(drawable)

        val compressedBitMap = ByteArrayOutputStream()
        val success: Boolean =
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, compressedBitMap)
        if (success) {
            val file = File(parent, id)
            file.outputStream().write(compressedBitMap.toByteArray())
            saver(id, target, file)
        } else Log.e(
            "ImageViewFilePersistor",
            "Error persisting user image. Cannot compress it"
        )
    }

}

class ImageViewBundlePersistor(parent: File) :
    ImageViewPersistor<Bundle>({ id, bundle, file -> bundle.putString(id, file.path) }, parent)

class ImageViewIntentPersistor(parent: File) :
    ImageViewPersistor<Intent>({ id, intent, file -> intent.putExtra(id, file.path) }, parent)

class ImageViewSharedPreferencesPersistor(parent: File) :
    ImageViewPersistor<SharedPreferences>(
        { id, storage, file ->
            with(storage.edit()) {
                putString(id, file.path);
                apply()
            }
        },
        parent
    )