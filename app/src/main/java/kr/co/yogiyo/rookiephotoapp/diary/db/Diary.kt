package kr.co.yogiyo.rookiephotoapp.diary.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "diaries")
class Diary(@field:PrimaryKey(autoGenerate = true)
            val idx: Int, @field:ColumnInfo(name = "date")
            var date: Date, @field:ColumnInfo(name = "image")
            var image: String?, @field:ColumnInfo(name = "description")
            var description: String?) {

    @Ignore
    constructor(date: Date, image: String?, description: String?) : this(0, date, image, description)

}
