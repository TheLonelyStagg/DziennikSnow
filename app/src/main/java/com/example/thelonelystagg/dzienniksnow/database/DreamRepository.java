package com.example.thelonelystagg.dzienniksnow.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import com.example.thelonelystagg.dzienniksnow.MainActivity;
import com.example.thelonelystagg.dzienniksnow.database.models.Dream;
import com.example.thelonelystagg.dzienniksnow.database.models.DreamTagJoin;
import com.example.thelonelystagg.dzienniksnow.database.models.Tag;

import java.util.ArrayList;
import java.util.List;

public class DreamRepository {
    private DreamDao mDreamDao;
    private TagDao mTagDao;
    private DreamTagJoinDao mDreamTagJoinDao;

    private LiveData<List<Dream>> mAllDreams;
    private LiveData<List<Tag>> mAllTags;


    DreamRepository(Application application)
    {
        DreamDataBase db = DreamDataBase.getDatabase(application);

        mDreamDao = db.dreamDao();
        mTagDao = db.tagDao();
        mDreamTagJoinDao = db.dreamTagJoinDao();

        mAllDreams = mDreamDao.getAllDreams();
        mAllTags = mTagDao.getAllTags();
    }

    LiveData<List<Dream>> getAllDreams () {return mAllDreams;}
    LiveData<List<Tag>> getAllTags () {return mAllTags;}

    List<Dream> getDreamsForTag(int tagId){ return mDreamTagJoinDao.getDreamsForTag(tagId);}
    List<Tag> getTagsForDream(int dreamId){ return mDreamTagJoinDao.getTagsForDream(dreamId);}
    List<Integer> getTagIdsForDream(int dreamId){return mDreamTagJoinDao.getTagIdsForDream(dreamId);}

    List<Tag> getAllTagsSynch() {return mTagDao.getAllTagsSynch();}


    //-------------------------------InsertTag-------------------------------------------------
    public void insertTag(Tag tag){
        new insertTagAsyncTask(mTagDao).execute(tag);
    }

    private static class insertTagAsyncTask extends AsyncTask<Tag, Void, Void> {
        private TagDao mAsyncTaskDao;

        insertTagAsyncTask(TagDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Tag... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    //-------------------------------InsertDream-------------------------------------------------
    public void insertDream(Dream dream){
        new insertDreamAsyncTask(mDreamDao).execute(dream);
    }

    private static class insertDreamAsyncTask extends AsyncTask<Dream, Void, Void> {
        private DreamDao mAsyncTaskDao;

        insertDreamAsyncTask(DreamDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Dream... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    //-------------------------------InsertDreamWithTags-------------------------------------------------

    private static class iDWT_TaskParams{
        Dream dream;
        List<Integer> listOfTagIds;

        iDWT_TaskParams(Dream dream, List<Integer> listOfTagIds) {
            this.dream = dream;
            this.listOfTagIds = listOfTagIds;
        }
    }

    public void insertDreamWithTags(Dream dream, List<Integer> listOfTagIds){
        iDWT_TaskParams params = new iDWT_TaskParams(dream,listOfTagIds);
        new insertDreamWithTagsAsyncTask(mDreamDao, mDreamTagJoinDao).execute(params);
    }

    private static class insertDreamWithTagsAsyncTask extends AsyncTask<iDWT_TaskParams, Void, Void> {
        private DreamDao mAsyncDreamDao;
        private DreamTagJoinDao mAsyncTagJoinDao;

        insertDreamWithTagsAsyncTask(DreamDao daoD, DreamTagJoinDao daoTJ) {
            mAsyncDreamDao = daoD;
            mAsyncTagJoinDao = daoTJ;
        }

        @Override
        protected Void doInBackground(final iDWT_TaskParams... params) {
           long insertedId = mAsyncDreamDao.insert(params[0].dream);

            List<Integer> tagIdsForDream = mAsyncTagJoinDao.getTagIdsForDream((int)insertedId);
            for (Integer id : params[0].listOfTagIds)
            {
                if(!tagIdsForDream.contains(id))
                {
                    DreamTagJoin newDTJ = new DreamTagJoin((int)insertedId, id);
                    mAsyncTagJoinDao.insert(newDTJ);
                }
            }
            return null;
        }
    }


//-------------------------------UpdateDreamWithTags-------------------------------------------------

    public void updateDreamWithTags(Dream dream, List<Integer> listOfTagIds){
        iDWT_TaskParams params = new iDWT_TaskParams(dream,listOfTagIds);
        new  updateDreamWithTagsAsyncTask(mDreamDao, mDreamTagJoinDao).execute(params);
    }

    private static class  updateDreamWithTagsAsyncTask extends AsyncTask<iDWT_TaskParams, Void, Void> {
        private DreamDao mAsyncDreamDao;
        private DreamTagJoinDao mAsyncTagJoinDao;

        updateDreamWithTagsAsyncTask(DreamDao daoD, DreamTagJoinDao daoTJ) {
            mAsyncDreamDao = daoD;
            mAsyncTagJoinDao = daoTJ;
        }

        @Override
        protected Void doInBackground(final iDWT_TaskParams... params) {
            mAsyncDreamDao.updateDream(params[0].dream);

            List<Integer> tagIdsForDream = mAsyncTagJoinDao.getTagIdsForDream(params[0].dream.id);
            for (Integer id : params[0].listOfTagIds)
            {
                if(!tagIdsForDream.contains(id))
                {
                    DreamTagJoin newDTJ = new DreamTagJoin(params[0].dream.id, id);
                    mAsyncTagJoinDao.insert(newDTJ);
                }
                else
                    tagIdsForDream.remove(tagIdsForDream.lastIndexOf(id));
            }

            if(!tagIdsForDream.isEmpty())
            {
                for (Integer id: tagIdsForDream)
                {
                    DreamTagJoin newDTJ= new DreamTagJoin(params[0].dream.id, id);
                    mAsyncTagJoinDao.deleteDreamTagJoin(newDTJ);
                }
            }

            return null;
        }
    }



//-------------------------------DeleteDream(with Dream-Tags)-------------------------------------------------

    public void deleteDream(Dream dream){
        new deleteDreamAcyncTask(mDreamDao, mDreamTagJoinDao).execute(dream);
    }

    private static class deleteDreamAcyncTask extends AsyncTask<Dream,Void,Void> {
        private DreamDao mAsyncDreamDao;
        private DreamTagJoinDao mAsyncTagJoinDao;

        deleteDreamAcyncTask(DreamDao daoD, DreamTagJoinDao daoTJ) {
            mAsyncDreamDao = daoD;
            mAsyncTagJoinDao = daoTJ;
        }

        @Override
        protected Void doInBackground(final Dream... params){
            Integer deletedId = params[0].id;
            mAsyncTagJoinDao.deleteAllOfDream(deletedId);
            mAsyncDreamDao.deleteDream(params[0]);


            return null;
        }


    }


    //-------------------------------DeleteTag(with Dream-Tags)-------------------------------------------------

    public void deleteTag(Tag tag){
        new deleteTagAcyncTask(mTagDao, mDreamTagJoinDao).execute(tag);
    }

    private static class deleteTagAcyncTask extends AsyncTask<Tag,Void,Void> {
        private TagDao mAsyncTagDao;
        private DreamTagJoinDao mAsyncTagJoinDao;

        deleteTagAcyncTask(TagDao daoT, DreamTagJoinDao daoTJ) {
            mAsyncTagDao = daoT;
            mAsyncTagJoinDao = daoTJ;
        }

        @Override
        protected Void doInBackground(final Tag... params){
            Integer deletedId = params[0].id;

            mAsyncTagDao.deleteTag(params[0]);
            mAsyncTagJoinDao.deleteAllOfTag(deletedId);

            return null;
        }
    }

    //-------------------------------DeleteCountZeroTags-------------------------------------------------

    public void deleteCountZeroTags(){
        new deleteCountZeroAcyncTask(mTagDao, mDreamTagJoinDao).execute();
    }

    private static class deleteCountZeroAcyncTask extends AsyncTask<Void,Void,Void> {
        private TagDao mAsyncTagDao;
        private DreamTagJoinDao mAsyncTagJoinDao;

        deleteCountZeroAcyncTask(TagDao daoT, DreamTagJoinDao daoTJ) {
            mAsyncTagDao = daoT;
            mAsyncTagJoinDao = daoTJ;
        }

        @Override
        protected Void doInBackground(final Void... params){

            List<Integer> listOfExistTagIds = mAsyncTagJoinDao.getAllTagIds();
            List<Integer> listOfAllTagIds = mAsyncTagDao.getAllTagIds();

            for (Integer tag: listOfAllTagIds)
            {
                if(!listOfExistTagIds.contains(tag))
                    mAsyncTagDao.deleteTagOfId(tag);
            }
            return null;
        }
    }

}
