package kr.co.yogiyo.rookiephotoapp.ui.camera.preview

import android.arch.lifecycle.ViewModel
import android.view.View
import kr.co.yogiyo.rookiephotoapp.GlobalApplication

class PreviewViewModel : ViewModel() {

    var isFromDiary: Boolean = GlobalApplication.globalApplicationContext.isFromDiary

    fun getAddDiaryVisibility(): Int = if (isFromDiary) View.INVISIBLE else View.VISIBLE

}