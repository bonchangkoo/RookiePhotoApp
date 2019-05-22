package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.UUID;

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

    @Ignore
    public Diary(Date date, @Nullable String image, @Nullable String description) {
        this(UUID.randomUUID().toString(), date, image, description);
    }

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
