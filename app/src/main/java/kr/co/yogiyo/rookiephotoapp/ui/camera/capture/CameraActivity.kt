package kr.co.yogiyo.rookiephotoapp.ui.camera.capture

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_camera.*
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.ui.base.BaseActivity
import java.util.ArrayList
import java.util.Calendar

class CameraActivity : BaseActivity() {

    private lateinit var cameraViewModel: CameraViewModel

    private var backPressedStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        setupCheckPermission()

        cameraViewModel = ViewModelProviders.of(this@CameraActivity).get(CameraViewModel::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(Bundle())
    }

    override fun onBackPressed() {
        when {
            cameraViewModel.timerCancel() -> return
            else -> {
                backPressedStartTime = Calendar.getInstance().timeInMillis.also {
                    if (it - backPressedStartTime < 2000 ||
                            GlobalApplication.globalApplicationContext.isFromDiary) {
                        super.onBackPressed()
                    } else {
                        showSnackbar(frame_container, getString(R.string.text_finish_snackbar))
                    }
                }
            }
        }
    }

    private fun setupCheckPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, CameraFragment.newInstance())
                        .commit()
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                finish()
            }
        }

        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
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