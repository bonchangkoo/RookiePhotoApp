package kr.co.yogiyo.rookiephotoapp.gallery

data class LoadImage(
        val pathOfImage: String,
        val modifiedDateOfImage: Long,
        var selected: Boolean = false)