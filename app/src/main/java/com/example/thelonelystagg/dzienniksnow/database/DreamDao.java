package com.example.thelonelystagg.dzienniksnow.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.thelonelystagg.dzienniksnow.database.models.Dream;

import java.util.List;

@Dao
public interface DreamDao {

    @Insert
    long insert(Dream dream);

    @Delete
    void deleteDream (Dream dream);

    @Update
    void updateDream(Dream dream);

    @Query("SELECT * FROM dream ORDER BY date DESC")
    LiveData<List<Dream>> getAllDreams();


}
