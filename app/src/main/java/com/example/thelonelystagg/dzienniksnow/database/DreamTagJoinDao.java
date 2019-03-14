package com.example.thelonelystagg.dzienniksnow.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.thelonelystagg.dzienniksnow.database.models.Dream;
import com.example.thelonelystagg.dzienniksnow.database.models.DreamTagJoin;
import com.example.thelonelystagg.dzienniksnow.database.models.Tag;

import java.util.List;

@Dao
public interface DreamTagJoinDao {
    @Insert
    void insert(DreamTagJoin dreamTagJoin);

    @Delete
    void deleteDreamTagJoin (DreamTagJoin dreamTagJoin);

    @Query("SELECT * FROM dream INNER JOIN dream_tag_join ON dream.id=dream_tag_join.dreamId WHERE dream_tag_join.tagId=:tagId")
    List<Dream> getDreamsForTag(final int tagId);

    @Query("SELECT * FROM tag INNER JOIN dream_tag_join ON tag.id=dream_tag_join.tagId WHERE dream_tag_join.dreamId=:dreamId")
    List<Tag> getTagsForDream(final int dreamId);

    @Query("SELECT dream_tag_join.tagId FROM tag INNER JOIN dream_tag_join ON tag.id=dream_tag_join.tagId WHERE dream_tag_join.dreamId=:dreamId")
    List<Integer> getTagIdsForDream(final int dreamId);

    @Query("DELETE FROM dream_tag_join WHERE dream_tag_join.dreamId=:dreamId")
    void deleteAllOfDream(final int dreamId);

    @Query("DELETE FROM dream_tag_join WHERE dream_tag_join.tagId=:tagId")
    void deleteAllOfTag(final int tagId);

    @Query("SELECT tagId FROM dream_tag_join")
    List<Integer> getAllTagIds();

}
