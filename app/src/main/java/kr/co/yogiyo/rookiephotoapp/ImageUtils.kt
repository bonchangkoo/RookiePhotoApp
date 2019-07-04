package kr.co.yogiyo.rookiephotoapp

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

@Throws(Exception::class)
fun Context?.bitmapToDownloads(bitmap: Bitmap): Boolean {

    if (!Constants.YOGIDIARY_PATH.exists()) {
        return (!Constants.YOGIDIARY_PATH.mkdirs())
    }

    val downloadsDirectoryPath = "${Constants.YOGIDIARY_PATH.path}/"
    val filename = String.format(Locale.getDefault(), "%d%s", Calendar.getInstance().timeInMillis, ".jpg")

    val file = File(downloadsDirectoryPath, filename)

    FileOutputStream(file).use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        it.flush()
    }

    this?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

    return true
}

fun Context?.saveBitmapToInternalStorage(bitmap: Bitmap) {

    val fileOutputStream: FileOutputStream?
    try {
        fileOutputStream = this?.openFileOutput("temp.jpg", Context.MODE_PRIVATE)
        fileOutputStream.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context?.copyFileToDownloads(croppedFileUri: Uri): Boolean {

    if (!Constants.YOGIDIARY_PATH.exists()) {
        return (!Constants.YOGIDIARY_PATH.mkdirs())
    }

    val downloadsDirectoryPath = Constants.YOGIDIARY_PATH.path + "/"
    val filename = String.format(Locale.getDefault(), "%d_%s", Calendar.getInstance().timeInMillis, croppedFileUri.lastPathSegment)

    val saveFile = File(downloadsDirectoryPath, filename)

    val inStream = FileInputStream(File(croppedFileUri.path))
    val outStream = FileOutputStream(saveFile)
    val inChannel = inStream.channel
    val outChannel = outStream.channel
    inChannel.transferTo(0, inChannel.size(), outChannel)
    inStream.close()
    outStream.close()

    this?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)))

    return true

}

// Bitmap을 Uri로 변환하는 함수
fun Context?.getImageUri(bitmap: Bitmap): Uri {
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(this?.contentResolver, bitmap, "Image", null)
    return Uri.parse(path)
}

fun queryImages(
        uri:Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection: Array<String>? = null, selection: String? = null,
        selectionArgs: Array<String>? = null, sortOrder: String? = null
): Cursor? {
    return GlobalApplication.globalApplicationContext.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
}