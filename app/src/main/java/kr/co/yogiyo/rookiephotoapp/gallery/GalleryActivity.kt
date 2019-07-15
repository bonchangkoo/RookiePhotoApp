package kr.co.yogiyo.rookiephotoapp.gallery

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_gallery.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.getFolderNames
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityGalleryBinding

import java.util.ArrayList

import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity
import java.io.File

class GalleryActivity : BaseActivity() {

    private val galleryViewModel by lazy {
        ViewModelProviders.of(this).get(GalleryViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun initView() {
        if (!GlobalApplication.globalApplicationContext.isFromDiary) {
            btn_done.visibility = View.GONE
        }

        btn_close.setOnClickListener { onBackPressed() }

        btn_edit.setOnClickListener {
            galleryViewModel.onClickEditButton()
        }

        btn_done.setOnClickListener {
            galleryViewModel.onClickDoneButton()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // TODO : Spinner 에 보여지는 텍스트 2가지로 구분하는게 좋을 것 같음 -> 폴더명... (이미지 수)
        spinner_gallery.run {
            adapter = ArrayAdapter(this@GalleryActivity,
                    android.R.layout.simple_spinner_item,
                    getFolderNames()).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val folderName = parent.getItemAtPosition(position).toString()
                            .substring(0, parent.getItemAtPosition(position).toString().lastIndexOf(" "))
                    galleryViewModel.onItemSelected(folderName)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing
                }
            }
        }
    }

    private fun GalleryViewModel.initViewModel() {
        startDoneButtonAction = { imagePath ->
            if (!File(imagePath).isFile) {
                showToast(R.string.toast_cannot_retrieve_selected_image)
            } else {
                Intent(this@GalleryActivity, DiaryEditActivity::class.java).apply {
                    data = Uri.fromFile(File(imagePath))
                    setResult(Activity.RESULT_OK, this)
                }
                finish()
            }
        }

        startEditButtonAction = { imagePath ->
            if (!File(imagePath).isFile) {
                showToast(R.string.toast_cannot_retrieve_selected_image)
            } else {
                val doStartEditPhotoActivityIntent = Intent(this@GalleryActivity, EditPhotoActivity::class.java).apply {
                    putExtra(getString(R.string.edit_photo_category_number), EDIT_SELECTED_PHOTO)
                    data = Uri.fromFile(File(imagePath))
                }
                if (GlobalApplication.globalApplicationContext.isFromDiary) {
                    startActivityForResult(doStartEditPhotoActivityIntent, Constants.REQUEST_DIARY_PICK_GALLERY)
                } else {
                    startActivity(doStartEditPhotoActivityIntent)
                    finish()
                }
            }
        }
    }

    private fun setupCheckPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                (DataBindingUtil.setContentView(this@GalleryActivity, R.layout.activity_gallery) as ActivityGalleryBinding)
                        .viewModel = galleryViewModel

                supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_gallery, GalleryFragment.newInstance())
                        .commit()

                initView()

                galleryViewModel.initViewModel()
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