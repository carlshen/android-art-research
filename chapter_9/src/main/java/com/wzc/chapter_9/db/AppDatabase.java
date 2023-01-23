package com.wzc.chapter_9.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Book.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String DB_NAME = "book.db";
    private static volatile AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (null == sInstance) {
            synchronized (AppDatabase.class) {
                if (null == sInstance) {
                    sInstance = Room.databaseBuilder(context, AppDatabase.class, DB_NAME).build();
                }
            }
        }
        return sInstance;
    }
    public abstract BookDao bookDao();
}
