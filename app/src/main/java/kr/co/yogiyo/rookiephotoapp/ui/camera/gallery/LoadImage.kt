package kr.co.yogiyo.rookiephotoapp.ui.camera.gallery

data class LoadImage(
        val pathOfImage: String,
        val modifiedDateOfImage: Long,
        var selected: Boolean = false)