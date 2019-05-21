package kr.co.yogiyo.rookiephotoapp.diary.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import kr.co.yogiyo.rookiephotoapp.diary.util.Converters;

@Database(entities = {Diary.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class DiaryDatabase extends RoomDatabase {
    public abstract DiaryDao diaryDao();

    private static DiaryDatabase INSTANCE;

    // TODO: database version 충돌 (기존 앱에서 수정하면)
    public static DiaryDatabase getInstance(final Context context) {
        synchronized (DiaryDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        DiaryDatabase.class, "Diaries.db")
                        .build();
            }
            return INSTANCE;
        }
    }
}
