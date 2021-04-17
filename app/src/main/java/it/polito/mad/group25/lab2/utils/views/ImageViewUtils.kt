package it.polito.mad.group25.lab2.utils.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

fun ImageView.fromFile(file: File) {
    val bytes = FileInputStream(file).readBytes()
    setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
    tag = file
}


fun ImageView.toFile(fileProvider: () -> File): File? {
    val drawable = drawable as BitmapDrawable?
    drawable?.bitmap?.let {
        val compressedBitMap = ByteArrayOutputStream()
        val success: Boolean =
            it.compress(Bitmap.CompressFormat.PNG, 0, compressedBitMap)
        if (success) {
            val file = fileProvider()
            file.outputStream().write(compressedBitMap.toByteArray())
            return file
        }
    }
    return null
}

fun ImageView.toFile(path: String): File? = toFile { File(path) }
fun ImageView.toFile(parent: File, path: String): File? = toFile { File(parent, path) }
fun ImageView.toFile(): File? = if (tag is File) toFile { tag as File } else null