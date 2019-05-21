package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

@Entity(tableName = "diaries")
public class Diary {

    @NonNull
    @PrimaryKey
    private final String idx;

    @ColumnInfo(name = "date")
    private Date date;

    @Nullable
    @ColumnInfo(name = "image")
    private String image;

    @Nullable
    @ColumnInfo(name = "description")
    private String description;

    /**
     * Use this constructor to create a new completed Diary.
     * // 머지할 때 UUID.randomUUID().toString() 사용할 예정입니다
     *
     * @param description description of the diary
     * @param image       image of the diary
     * @param date        date of created diary
     */
    @Ignore
    public Diary(int idx, Date date, @Nullable String image, @Nullable String description) {
        this(String.valueOf(idx), date, image, description);
    }

    /**
     * Use this constructor to specify a completed Task if the Task already has an id (copy of
     * another Task).
     *
     * @param description description of the diary
     * @param image       image of the diary
     * @param idx         id of the diary
     * @param date        date of created diary
     */

    public Diary(@NonNull String idx, Date date, @Nullable String image, @Nullable String description) {
        this.idx = idx;
        this.date = date;
        this.image = image;
        this.description = description;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setImage(@Nullable String image) {
        this.image = image;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public String getIdx() {
        return idx;
    }

    public Date getDate() {
        return date;
    }

    @Nullable
    public String getImage() {
        return image;
    }

    @Nullable
    public String getDescription() {
        return description;
    }


}
