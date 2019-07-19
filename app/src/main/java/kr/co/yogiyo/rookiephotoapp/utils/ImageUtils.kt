package kr.co.yogiyo.rookiephotoapp.utils

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.Constants.FOONCARE_PATH
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

@Throws(Exception::class)
fun Context?.bitmapToDownloads(bitmap: Bitmap, imageFileName: Long? = null): Boolean {

    if (!Constants.FOONCARE_PATH.exists()) {
        if (!Constants.FOONCARE_PATH.mkdirs()) {
            return false
        }
    }

    val downloadsDirectoryPath = "${FOONCARE_PATH.path}/"
    val timeValueOfFileName: Long = imageFileName ?: Calendar.getInstance().timeInMillis

    val filename = String.format(Locale.getDefault(), "%d%s", timeValueOfFileName, ".jpg")
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


fun Context?.loadBitmapFromInternalStorage(): Bitmap? {
    val fileInputStream: FileInputStream
    var bitmap: Bitmap? = null
    try {
        fileInputStream = this!!.openFileInput("temp.jpg")
        bitmap = BitmapFactory.decodeStream(fileInputStream)
        fileInputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return bitmap
}

fun Context?.copyFileToDownloads(croppedFileUri: Uri, imageFileName: Long? = null): Boolean {

    if (!Constants.FOONCARE_PATH.exists()) {
        if (!Constants.FOONCARE_PATH.mkdirs()) {
            return false
        }
    }

    val downloadsDirectoryPath = Constants.FOONCARE_PATH.path + "/"
    val timeValueOfFileName = imageFileName ?: Calendar.getInstance().timeInMillis

    val fileName = if (imageFileName != null) String.format(Locale.getDefault(), "%d%s", timeValueOfFileName, ".jpg") else {
        String.format(Locale.getDefault(), "%d_%s", timeValueOfFileName, croppedFileUri.lastPathSegment)
    }

    val saveFile = File(downloadsDirectoryPath, fileName)

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
        uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection: Array<String>? = null, selection: String? = null,
        selectionArgs: Array<String>? = null, sortOrder: String? = null
): Cursor? {
    return GlobalApplication.globalApplicationContext.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
}