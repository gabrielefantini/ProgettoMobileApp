package it.polito.mad.group25.lab.utils.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

fun ImageView.fromFile(file: File) {
    if (file.exists()) {
        val bytes = FileInputStream(file).readBytes()
        setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
    }
    tag = file
}

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

fun ImageView.toFile(fileProvider: () -> File): File? {
    val drawable = drawable
    val bitmap: Bitmap = if (drawable is BitmapDrawable)
        drawable.bitmap else getBitmapFromDrawable(drawable)

    val compressedBitMap = ByteArrayOutputStream()
    val success: Boolean =
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, compressedBitMap)
    if (success) {
        val file = fileProvider()
        file.outputStream().write(compressedBitMap.toByteArray())
        return file
    }
    return null
}

fun ImageView.toFile(path: String): File? = toFile { File(path) }
fun ImageView.toFile(parent: File, path: String): File? = toFile { File(parent, path) }
fun ImageView.toFile(): File? = if (tag is File) toFile { tag as File } else null