package com.example.thelonelystagg.dzienniksnow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.thelonelystagg.dzienniksnow.database.TagsViewModel;
import com.example.thelonelystagg.dzienniksnow.database.models.Dream;
import com.example.thelonelystagg.dzienniksnow.database.models.Tag;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShowDreamActivity extends AppCompatActivity {
    private Menu mojeMenu;

    static final int DELETE_DREAM = 20;
    private EditText mET_dreamTitle,
            mET_recordingSource,
            mET_dreamDesc,
            mET_date,
            mET_time,
            mET_tags;
    private TextView mTV_isRecording;

    private Button mB_toRecording;


    private int cDay;
    private int cMonth;
    private int cYear;
    private int cHour;
    private int cMinute;

    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
    private AudioManager mAudiomanager;
    String src;
    private boolean isRecording = false;
    private boolean isPlaying = false;

    private ArrayList<Integer> indexesOfChosenTags;
    private List<Tag> listOfAllTags;
    private TagsViewModel mTagsViewModel;

    private Context mainWindowContext;
    Dream originalDream;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_dream);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        originalDream = (Dream) intent.getSerializableExtra("NewDream");
        indexesOfChosenTags = (ArrayList<Integer>) intent.getSerializableExtra("ListOfTags");

        mainWindowContext = this;


        mTagsViewModel = ViewModelProviders.of(this).get(TagsViewModel.class);

        mTagsViewModel.getAllTags().observe(this, new Observer<List<Tag>>() {
            @Override
            public void onChanged(@Nullable List<Tag> tags) {
                listOfAllTags = tags;

                if(mET_tags.getText().toString().isEmpty()&& listOfAllTags.size()!=0)
                {
                    String outText= "";
                    if(indexesOfChosenTags.size()!=0)
                    {
                        for (Tag tag: listOfAllTags)
                        {
                            if(indexesOfChosenTags.contains(tag.getId()))
                                outText = outText+tag.getName() + ", ";
                        }
                    }
                    if(outText.endsWith(", "))
                        outText = outText.substring(0, outText.length()-2);
                    mET_tags.setText(outText);
                }
            }
        });



        int foo;
        String tmpDate = originalDream.getDate();
        //"YYYY-MM-dd HH:mm:ss.SSSS"
        try {
            foo = Integer.parseInt(tmpDate.substring(8,10));
        }
        catch (NumberFormatException e)
        {
            foo = 0;
        }
        cDay = foo;

        try {
            foo = Integer.parseInt(tmpDate.substring(5,7));
        }
        catch (NumberFormatException e)
        {
            foo = 0;
        }
        cMonth = foo;

        try {
            foo = Integer.parseInt(tmpDate.substring(0,4));
        }
        catch (NumberFormatException e)
        {
            foo = 0;
        }
        cYear = foo;

        try {
            foo = Integer.parseInt(tmpDate.substring(11,13));
        }
        catch (NumberFormatException e)
        {
            foo = 0;
        }
        cHour = foo;

        try {
            foo = Integer.parseInt(tmpDate.substring(14,16));
        }
        catch (NumberFormatException e)
        {
            foo = 0;
        }
        cMinute = foo;

        mET_date = findViewById(R.id.editTxt_date);
        mET_dreamDesc = findViewById(R.id.editTxt_dreamDesc);
        mET_dreamTitle = findViewById(R.id.editTxt_dreamTitle);
        mET_recordingSource = findViewById(R.id.editTxt_recordingSource);
        mET_time = findViewById(R.id.editTxt_time);
        mET_tags = findViewById(R.id.editTxt_tags);
        mTV_isRecording = findViewById(R.id.switch1);

        mB_toRecording = findViewById(R.id.button);

        updateET_date();
        updateET_time();



        mET_dreamTitle.setText(originalDream.getTitle());
        mET_dreamTitle.setCursorVisible(false);
        mET_dreamTitle.setFocusable(false);
        mET_dreamTitle.setFocusableInTouchMode(false);

        mET_recordingSource.setText(originalDream.getAudioFileSrc());
        mET_recordingSource.setCursorVisible(false);
        mET_recordingSource.setFocusable(false);
        mET_recordingSource.setFocusableInTouchMode(false);
        mET_recordingSource.setVisibility(View.GONE);

        mET_dreamDesc.setText(originalDream.getDescription());
        mET_dreamDesc.setCursorVisible(false);
        mET_dreamDesc.setFocusable(false);
        mET_dreamDesc.setFocusableInTouchMode(false);


        if (!originalDream.getIfAudioAsBoolean())
        {
            mET_recordingSource.setVisibility(View.GONE);
            mB_toRecording.setVisibility(View.GONE);
            mTV_isRecording.setVisibility(View.GONE);
        }



        mB_toRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(v.getContext());
                final View mView = getLayoutInflater().inflate(R.layout.dialog_record, null);
                final String start_src = mET_recordingSource.getText().toString();
                src = start_src;
                Calendar c = Calendar.getInstance();
                Date newDate = c.getTime();
                String newSrcName="";
                DateFormat formattu = new SimpleDateFormat("YYYY-MM-dd-HH-mm-ss-SSSS", Locale.ENGLISH);
                if(newDate!=null)
                {
                    newSrcName = formattu.format(newDate);
                }

                final boolean isRecording = false;
                final boolean isPlaying = false;

                final ImageButton ad_recordBtn = mView.findViewById(R.id.recordBtn);
                final ImageButton ad_pauseBtn = mView.findViewById(R.id.pauseBtn);
                final ImageButton ad_playBtn = mView.findViewById(R.id.playBtn);
                final ImageButton ad_stopRecBtn = mView.findViewById(R.id.stopRecBtn);
                final Button ad_saveBtn = mView.findViewById(R.id.saveBtn);
                final Button ad_closeBtn = mView.findViewById(R.id.closeBtn);
                final TextView ad_replayTV = mView.findViewById(R.id.textView2);
                final TextView ad_recordTV = mView.findViewById(R.id.textView);

                ad_recordBtn.setVisibility(View.GONE);
                ad_stopRecBtn.setVisibility(View.GONE);
                ad_recordTV.setVisibility(View.GONE);
                ad_saveBtn.setVisibility(View.GONE);

                ad_pauseBtn.setVisibility(View.INVISIBLE);
                ad_stopRecBtn.setVisibility(View.INVISIBLE);

                if(src == null || src.trim().isEmpty())
                {
                    ad_playBtn.setVisibility(View.GONE);
                    ad_pauseBtn.setVisibility(View.GONE);
                    ad_replayTV.setVisibility(View.GONE);
                    src = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/"+newSrcName+".3gp";
                }
                mAudiomanager = (AudioManager)getSystemService(Context.AUDIO_SERVICE) ;


                ad_playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad_playBtn.setVisibility(View.INVISIBLE);
                        ad_pauseBtn.setVisibility(View.VISIBLE);
                        ad_closeBtn.setEnabled(false);
                        playAudio();
                    }
                });

                ad_pauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad_pauseBtn.setVisibility(View.INVISIBLE);
                        ad_playBtn.setVisibility(View.VISIBLE);
                        ad_closeBtn.setEnabled(true);
                        pausedAudio();
                    }
                });
                mBuilder.setView(mView);

                final AlertDialog alertDialog = mBuilder.create();

                ad_closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });


                alertDialog.show();
            }
        });

        mET_date.setCursorVisible(false);
        mET_date.setFocusable(false);
        mET_date.setFocusableInTouchMode(false);



        mET_time.setCursorVisible(false);
        mET_time.setFocusable(false);
        mET_time.setFocusableInTouchMode(false);


        mET_tags.setCursorVisible(false);
        mET_tags.setFocusable(false);
        mET_tags.setFocusableInTouchMode(false);



    }



    protected boolean hasMicrophone(){
        PackageManager packageManager = this.getPackageManager();
        return packageManager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);

    }


    public void pausedAudio ()
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public void playAudio()
    {
        isPlaying =true;

        mediaPlayer = new MediaPlayer();
        try{
            mediaPlayer.setDataSource(src);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e("TMP", "prepare() failed");
        }


    }

    private void updateET_date(){
        mET_date.setText(new StringBuilder()
                // Month is 0 based so add 1
                .append((cDay<10)?"0"+cDay:cDay).append(" / ").append(((cMonth+1)<10)?"0"+(cMonth+1):(cMonth + 1)).append(" / ").append(cYear));
    }

    private void updateET_time(){
        mET_time.setText(new StringBuilder()
                // Month is 0 based so add 1
                .append(cHour).append(" : ").append((cMinute<10)?"0"+cMinute:cMinute));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show, menu);
        mojeMenu = menu;

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Dream resultDream = originalDream;
        Intent replyIntent = new Intent();
        replyIntent.putExtra("NewDream", resultDream);
        replyIntent.putExtra("ListOfTags", indexesOfChosenTags);
        switch(item.getItemId()){
            case R.id.action_delete:

                setResult(DELETE_DREAM, replyIntent);
                finish();
                return true;
            case R.id.action_edit:

                setResult(RESULT_OK, replyIntent);
                finish();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

}
