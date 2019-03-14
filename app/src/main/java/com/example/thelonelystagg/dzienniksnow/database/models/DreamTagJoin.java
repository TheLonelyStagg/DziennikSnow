package com.example.thelonelystagg.dzienniksnow.database.models;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

@Entity(tableName = "dream_tag_join",
            primaryKeys = {"dreamId", "tagId"},
            foreignKeys = {
                    @ForeignKey(entity = Dream.class,
                                parentColumns = "id",
                                childColumns = "dreamId"),
                    @ForeignKey(entity = Tag.class,
                                parentColumns = "id",
                                childColumns = "tagId")
            })
public class DreamTagJoin {

    public final int dreamId;
    public final int tagId;

    public DreamTagJoin(int dreamId, int tagId) {
        this.dreamId = dreamId;
        this.tagId = tagId;
    }

    public int getDreamId() {
        return dreamId;
    }

    public int getTagId() {
        return tagId;
    }
}
