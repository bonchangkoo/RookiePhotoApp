package kr.co.yogiyo.rookiephotoapp.ui.camera.gallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_gallery.*
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.ui.base.BaseActivity
import kr.co.yogiyo.rookiephotoapp.ui.camera.edit.EditPhotoActivity
import kr.co.yogiyo.rookiephotoapp.ui.diary.edit.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.utils.queryImages
import java.util.*

class GalleryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        setupCheckPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        intent?.run {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == Constants.REQUEST_DIARY_PICK_GALLERY) {
                    Intent(this@GalleryActivity, DiaryEditActivity::class.java)
                            .setData(data).let {
                                setResult(Activity.RESULT_OK, it)
                            }
                    finish()
                }
            }
        }
    }

    fun setControlButtonEnabled(selectedImage: Boolean) {
        btn_edit.run {
            isEnabled = selectedImage
            alpha = if (selectedImage) 1.0F else 0.5F
        }
        btn_done.run {
            isEnabled = selectedImage
            alpha = if (selectedImage) 1.0F else 0.5F
        }
    }

    private fun getFolderNames(): List<String> {
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
                    set(0, String.format(getString(R.string.spinner_folder_name_count),
                            key, mapOfAllImageFolders[key]))
                } else {
                    add(String.format(getString(R.string.spinner_folder_name_count),
                            key, mapOfAllImageFolders[key])
                    )
                }
            }
            add(String.format(getString(R.string.spinner_folder_name_count),
                    getString(R.string.spinner_folder_all), countOfAllImages))
        }
    }

    private fun initView() {
        if (!GlobalApplication.globalApplicationContext.isFromDiary) {
            btn_done.visibility = View.GONE
        }

        btn_close.setOnClickListener { onBackPressed() }

        btn_edit.setOnClickListener {
            supportFragmentManager.findFragmentById(R.id.frame_gallery).let { nowFragment ->
                if (nowFragment is GalleryFragment) {
                    nowFragment.selectedImageUri?.let { uriForEdit ->
                        setControlButtonEnabled(false)
                        val doStartEditPhotoActivityIntent = Intent(this@GalleryActivity, EditPhotoActivity::class.java).apply {
                            putExtra(getString(R.string.edit_photo_category_number), EDIT_SELECTED_PHOTO)
                            data = uriForEdit
                        }
                        if (GlobalApplication.globalApplicationContext.isFromDiary) {
                            startActivityForResult(doStartEditPhotoActivityIntent, Constants.REQUEST_DIARY_PICK_GALLERY)
                        } else {
                            startActivity(doStartEditPhotoActivityIntent)
                            finish()
                        }
                    } ?: showToast(R.string.toast_cannot_retrieve_selected_image)
                }
            }
        }

        btn_done.setOnClickListener {
            supportFragmentManager.findFragmentById(R.id.frame_gallery).let { nowFragment ->
                if (nowFragment is GalleryFragment) {
                    nowFragment.selectedImageUri?.let { originalUri ->
                        Intent(this@GalleryActivity, DiaryEditActivity::class.java).apply {
                            data = originalUri
                            setResult(Activity.RESULT_OK, this)
                        }
                        finish()
                    } ?: showToast(R.string.toast_cannot_retrieve_selected_image)
                }
            }
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // TODO : Spinner 에 보여지는 텍스트 2가지로 구분하는게 좋을 것 같음 -> 폴더명... (이미지 수)
        val gallerySpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, getFolderNames()).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinner_gallery.run {
            adapter = gallerySpinnerAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    setControlButtonEnabled(false)

                    val item = parent.getItemAtPosition(position).toString().substring(0, parent.getItemAtPosition(position).toString().lastIndexOf(" "))

                    supportFragmentManager.beginTransaction()
                            .replace(R.id.frame_gallery, GalleryFragment.newInstance(item))
                            .commit()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing
                }
            }
        }
    }

    private fun setupCheckPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                initView()
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                finish()
            }
        }

        val permissions = mutableListOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage(R.string.text_denied_permission)
                .setGotoSettingButtonText(R.string.text_settings)
                .setDeniedCloseButtonText(R.string.text_cancel)
                .setPermissions(*permissions.toTypedArray())
                .check()
    }
}