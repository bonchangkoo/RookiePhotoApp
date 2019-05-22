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
    private final String id;

    @ColumnInfo(name = "date")
    private final Date date;

    @Nullable
    @ColumnInfo(name = "description")
    private final String description;

    @Nullable
    @ColumnInfo(name = "image")
    private final String image;

    /**
     * Use this constructor to create a new Diary.
     *
     * @param description description of the diary
     * @param image       image url as string of the diary
     */
    @Ignore
    public Diary(@Nullable String description, @Nullable String image) {
        this(description, image, UUID.randomUUID().toString(), new Date());
    }

    /**
     * Use this constructor to create an Diary if the Diary already has an id (copy of another
     * Diary).
     *
     * @param description description of the diary
     * @param image       image url as string of the diary
     * @param id          id of the diary
     */
    @Ignore
    public Diary(@Nullable String description, @Nullable String image, @NonNull String id) {
        this(description, image, id, new Date());
    }

    /**
     * Use this constructor to create a new completed Diary.
     *
     * @param description description of the diary
     * @param image       image url as string of the diary
     * @param date    date of created diary
     */
    @Ignore
    public Diary(@Nullable String description, @Nullable String image, Date date) {
        this(description, image, UUID.randomUUID().toString(), date);
    }

    /**
     * Use this constructor to specify a completed Task if the Task already has an id (copy of
     * another Task).
     *
     * @param description description of the diary
     * @param image       image url as string of the diary
     * @param id          id of the diary
     * @param date    date of created diary
     */
    public Diary(@Nullable String description, @Nullable String image,
                @NonNull String id, Date date) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.image = image;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getImage() {
        return image;
    }
}