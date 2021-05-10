package it.polito.mad.group25.lab.utils.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.google.firebase.firestore.Blob
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

fun ImageView.fromFile(file: File) {
    if (file.exists()) {
        val bytes = FileInputStream(file).readBytes()
        setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
    }
    tag = file
}

fun ImageView.fromBytes(bytes: ByteArray) {
    setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
}

fun ImageView.fromByteList(byteList: List<Byte>) = fromBytes(byteList.toByteArray())

fun ImageView.fromBlob(blob: Blob) = fromBytes(blob.toBytes())

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

private fun writeBitmapOnStream(bitmap: Bitmap, outputStream: OutputStream) {
    val compressedBitMap = ByteArrayOutputStream()
    val success: Boolean =
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, compressedBitMap)
    if (success) {
        outputStream.write(compressedBitMap.toByteArray())
    }
}

private fun convertBitmapToBytes(bitmap: Bitmap): ByteArray =
    ByteArrayOutputStream().apply {
        writeBitmapOnStream(bitmap, this)
    }.toByteArray()

fun ImageView.toBitmap() = getBitmapFromDrawable(this.drawable)
fun ImageView.toBytes(): ByteArray = toBitmap().let(::convertBitmapToBytes)
fun ImageView.toByteList(): List<Byte> = toBytes().toList()
fun ImageView.toBlob(): Blob = Blob.fromBytes(toBytes())

fun ImageView.toFile(fileProvider: () -> File) = fileProvider().apply {
    val drawable = drawable
    val bitmap: Bitmap = if (drawable is BitmapDrawable)
        drawable.bitmap else getBitmapFromDrawable(drawable)
    writeBitmapOnStream(bitmap, this.outputStream())
}


fun ImageView.toFile(path: String): File? = toFile { File(path) }
fun ImageView.toFile(parent: File, path: String): File? = toFile { File(parent, path) }
fun ImageView.toFile(): File? = if (tag is File) toFile { tag as File } else null
