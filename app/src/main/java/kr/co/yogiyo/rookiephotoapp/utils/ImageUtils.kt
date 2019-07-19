package kr.co.yogiyo.rookiephotoapp.utils

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kr.co.yogiyo.rookiephotoapp.ui.camera.gallery.LoadImage
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale
import java.util.Calendar

@Throws(Exception::class)
fun Context?.bitmapToDownloads(bitmap: Bitmap): Boolean {

    if (!Constants.FOONCARE_PATH.exists()) {
        if (!Constants.FOONCARE_PATH.mkdirs()) {
            return false
        }
    }

    val downloadsDirectoryPath = "${Constants.FOONCARE_PATH.path}/"
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

    if (!Constants.FOONCARE_PATH.exists()) {
        return (!Constants.FOONCARE_PATH.mkdirs())
    }

    val downloadsDirectoryPath = Constants.FOONCARE_PATH.path + "/"

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
        uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection: Array<String>? = null, selection: String? = null,
        selectionArgs: Array<String>? = null, sortOrder: String? = null
): Cursor? {
    return GlobalApplication.globalApplicationContext.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
}

fun loadImages(folderName: String?): List<LoadImage> {
    val listOfAllImages = ArrayList<LoadImage>()

    queryImages(projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    ))?.run {
        val columnIndexData = getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        val columnIndexDateModified = getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
        val columnIndexFolderName = getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        while (moveToNext()) {
            if (getString(columnIndexFolderName) == folderName || folderName == "All") {
                listOfAllImages.add(LoadImage(getString(columnIndexData), getLong(columnIndexDateModified)))
            }
        }

        close()

    } ?: return listOfAllImages

    return sortImages(listOfAllImages, true)
}

fun sortImages(images: List<LoadImage>, reverse: Boolean): List<LoadImage> {
    return if (reverse) {
        images.sortedByDescending { loadImage -> loadImage.modifiedDateOfImage }
    } else {
        images.sortedBy { loadImage -> loadImage.modifiedDateOfImage }
    }
}

fun getFolderNames(): List<String> {
    val mapOfAllImageFolders = HashMap<String, Int>()
    var countOfAllImages = 0

    queryImages(projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))?.run {
        val columnIndexFolderName = getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        while (moveToNext()) {
            countOfAllImages++
            getString(columnIndexFolderName).let { folderName ->
                mapOfAllImageFolders[folderName] = mapOfAllImageFolders[folderName]?.plus(1)
                        ?: 1
            }
        }

        close()
    } ?: return ArrayList()

    return ArrayList<String>().apply {
        if (mapOfAllImageFolders.containsKey("FooNCaRe")) {
            add("")
        }

        for (key in mapOfAllImageFolders.keys) {
            if (key == "FooNCaRe") {
                set(0, "$key (${mapOfAllImageFolders[key]})")
            } else {
                add("$key (${mapOfAllImageFolders[key]})")
            }
        }
        add("All ($countOfAllImages)")
    }
}