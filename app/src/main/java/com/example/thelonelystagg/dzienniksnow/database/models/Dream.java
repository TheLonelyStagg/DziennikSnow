package com.example.thelonelystagg.dzienniksnow.database.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Entity
public class Dream implements Serializable {


    @PrimaryKey (autoGenerate = true)
    public final int id;

    public final String title;
    public final int ifAudio;
        //ifAudio jako 1 - true lub 0 - false
    public final String audioFileSrc;
    public final String description;
    public final String date;
        //data jako string 'YYYY-MM-DD HH:mm:ss.ssss'

    public Dream(int id, String title, int ifAudio, String audioFileSrc, String description, String date) {
        this.id = id;
        this.title = title;
        this.ifAudio = ifAudio;
        this.audioFileSrc = audioFileSrc;
        this.description = description;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getIfAudio() {
        return ifAudio;
    }

    public String getAudioFileSrc() {
        return audioFileSrc;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public Date getDateAsDate() {
        DateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSSS", Locale.ENGLISH);
        Date dateOut = null;
        try{
            dateOut = format.parse(date);
        }catch (ParseException e){
            Log.d("KAPPA", "Exep in parseDate from string to Date");
        }
        return dateOut;
    }

    public Boolean getIfAudioAsBoolean() {
        return ifAudio==1;
    }
}
