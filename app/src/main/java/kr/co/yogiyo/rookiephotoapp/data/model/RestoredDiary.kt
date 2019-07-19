package kr.co.yogiyo.rookiephotoapp.data.model

import com.google.gson.annotations.SerializedName

data class RestoredDiary(

        @SerializedName("id")
        var id: Int,
        @SerializedName("diary_id")
        var diaryId: Int,
        @SerializedName("datetime")
        var datetime: String,
        @SerializedName("description")
        var description: String? = null,
        @SerializedName("image")
        var image: String? = null,
        @SerializedName("created_at")
        var createdAt: String,
        @SerializedName("diary_user")
        var diaryUser: Int
)