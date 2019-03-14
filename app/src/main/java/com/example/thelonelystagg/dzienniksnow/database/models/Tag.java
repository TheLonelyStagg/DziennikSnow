package com.example.thelonelystagg.dzienniksnow.database.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Tag implements Serializable {
    @PrimaryKey (autoGenerate = true)
    public final int id;

    public final String name;

    public Tag(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
