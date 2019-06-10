package kr.co.yogiyo.rookiephotoapp.camera.capture

import android.content.Context
import android.content.Intent
import android.databinding.BaseObservable
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kr.co.yogiyo.rookiephotoapp.Constants
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class PreviewViewModel : BaseObservable() {

    @Throws(Exception::class)
     fun bitmapToDownloads(context: Context, bitmap: Bitmap) {
        if (!Constants.YOGIDIARY_PATH.exists()) {
            if (!Constants.YOGIDIARY_PATH.mkdirs()) {
                //return finish()
            }
        }

        val downloadsDirectoryPath = Constants.YOGIDIARY_PATH.path + "/"
        val filename = String.format(Locale.getDefault(), "%d%s", Calendar.getInstance().timeInMillis, ".jpg")

        val file = File(downloadsDirectoryPath, filename)

        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            it.flush()
        }

        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap) {

        var fileOutputStream: FileOutputStream?
        try {
            fileOutputStream = context.openFileOutput("temp.jpg", Context.MODE_PRIVATE)
            fileOutputStream.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Bitmap을 Uri로 변환하는 함수
    fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Image", null)
        return Uri.parse(path)
    }
}