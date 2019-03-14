package com.example.thelonelystagg.dzienniksnow.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.thelonelystagg.dzienniksnow.database.models.Dream;

import java.util.List;

public class DreamsViewModel extends AndroidViewModel {

    private DreamRepository mRepository;
    private LiveData<List<Dream>> mAllDreams;


    public DreamsViewModel(Application application) {
        super(application);
        mRepository = new DreamRepository(application);

        mAllDreams = mRepository.getAllDreams();
    }

    public LiveData<List<Dream>> getAllDreams () {return mAllDreams;}
    public List<Integer> getTagIdsForDream(int dreamId) {return mRepository.getTagIdsForDream(dreamId);}

    public void insertDream(Dream dream, List<Integer> listTags) { mRepository.insertDreamWithTags(dream,listTags);}
    public void updateDream(Dream dream, List<Integer> listTags) { mRepository.updateDreamWithTags(dream,listTags);}
    public void cleanTagsTable(){mRepository.deleteCountZeroTags();}
    public void deleteDream(Dream dream){ mRepository.deleteDream(dream);}


}
