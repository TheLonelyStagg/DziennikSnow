package com.example.thelonelystagg.dzienniksnow.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.thelonelystagg.dzienniksnow.database.models.Tag;

import java.util.List;

@Dao
public interface TagDao {
    @Insert
    long insert(Tag dream);

    @Delete
    void deleteTag (Tag dream);

    @Update
    void updateTag(Tag dream);

    @Query("SELECT * FROM tag ORDER BY name DESC")
    LiveData<List<Tag>> getAllTags();

    @Query("SELECT * FROM tag ORDER BY name DESC")
    List<Tag> getAllTagsSynch();


    @Query("SELECT id FROM tag")
    List<Integer> getAllTagIds();

    @Query("DELETE FROM tag WHERE tag.id=:tagId ")
    void deleteTagOfId(int tagId);
}
