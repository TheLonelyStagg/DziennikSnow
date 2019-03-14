package com.example.thelonelystagg.dzienniksnow.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.thelonelystagg.dzienniksnow.database.models.Tag;

import java.util.List;

public class TagsViewModel extends AndroidViewModel {

    private DreamRepository mRepository;
    private LiveData<List<Tag>> mAllTags;

    public TagsViewModel (Application application)
    {
        super(application);
        mRepository = new DreamRepository(application);

        mAllTags = mRepository.getAllTags();
        getAllTags();
    }

    public LiveData<List<Tag>> getAllTags () {return mAllTags;}
    public List<Tag> getAllTagsSynch() {return mRepository.getAllTagsSynch();}

    public void insertTag(Tag tag) {mRepository.insertTag(tag);}
    public List<Integer> getTagIdsForDream(int dreamId) {return mRepository.getTagIdsForDream(dreamId);}



}
