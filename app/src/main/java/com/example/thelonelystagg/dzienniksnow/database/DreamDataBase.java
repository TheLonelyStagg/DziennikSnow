package com.example.thelonelystagg.dzienniksnow.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.thelonelystagg.dzienniksnow.database.models.Dream;
import com.example.thelonelystagg.dzienniksnow.database.models.DreamTagJoin;
import com.example.thelonelystagg.dzienniksnow.database.models.Tag;


@Database(entities = {Dream.class, Tag.class, DreamTagJoin.class},
            version = 1)
public abstract class DreamDataBase extends RoomDatabase {

    public abstract DreamDao dreamDao();
    public abstract TagDao tagDao();
    public abstract DreamTagJoinDao dreamTagJoinDao();

    private static volatile DreamDataBase INSTANCE;

    static DreamDataBase getDatabase(final Context context) {
        if (INSTANCE == null){
            synchronized (DreamDataBase.class) {
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DreamDataBase.class, "dream_database")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }



}
