package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Diary.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class DiaryDatabase extends RoomDatabase {

    public abstract DiaryDao diaryDao();

    private static volatile DiaryDatabase INSTANCE;

    public static DiaryDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DiaryDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DiaryDatabase.class, "Diaries.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
