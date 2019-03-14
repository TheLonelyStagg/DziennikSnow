package com.example.thelonelystagg.dzienniksnow;

import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
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
import android.widget.Toast;

import com.example.thelonelystagg.dzienniksnow.R;
import com.example.thelonelystagg.dzienniksnow.database.TagsViewModel;
import com.example.thelonelystagg.dzienniksnow.database.models.Dream;
import com.example.thelonelystagg.dzienniksnow.database.models.Tag;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddDreamActivity extends AppCompatActivity {

    private Menu mojeMenu;

    private EditText mET_dreamTitle,
             mET_recordingSource,
             mET_dreamDesc,
             mET_date,
             mET_time,
             mET_tags;

    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
    private AudioManager mAudiomanager;
    String src;

    private Switch mS_isRecording;
    private Button mB_toRecording;

    private boolean isRecording = false;
    private boolean isPlaying = false;

    private int cDay;
    private int cMonth;
    private int cYear;
    private int cHour;
    private int cMinute;

    private ArrayList<Integer> indexesOfChosenTags;
    private List<Tag> listOfAllTags;
    private TagsViewModel mTagsViewModel;

    private Context mainWindowContext;

    protected boolean hasMicrophone(){
        PackageManager packageManager = this.getPackageManager();
        return packageManager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dream);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainWindowContext = this;

        final Calendar calendar = Calendar.getInstance();

        indexesOfChosenTags = new ArrayList<>();

        mTagsViewModel = ViewModelProviders.of(this).get(TagsViewModel.class);

        mTagsViewModel.getAllTags().observe(this, new Observer<List<Tag>>() {
                    @Override
                    public void onChanged(@Nullable List<Tag> tags) {
                        listOfAllTags = tags;
                    }
                });

        cDay = calendar.get(Calendar.DAY_OF_MONTH);
        cMonth = calendar.get(Calendar.MONTH);
        cYear = calendar.get(Calendar.YEAR);

        cHour = calendar.get(Calendar.HOUR_OF_DAY);
        cMinute = calendar.get(Calendar.MINUTE);

        mET_date = findViewById(R.id.editTxt_date);
        mET_dreamDesc = findViewById(R.id.editTxt_dreamDesc);
        mET_dreamTitle = findViewById(R.id.editTxt_dreamTitle);
        mET_recordingSource = findViewById(R.id.editTxt_recordingSource);
        mET_time = findViewById(R.id.editTxt_time);
        mET_tags = findViewById(R.id.editTxt_tags);
        mS_isRecording = findViewById(R.id.switch1);
            mS_isRecording.setChecked(true);
        mB_toRecording = findViewById(R.id.button);

        updateET_date();
        updateET_time();



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
                if (!hasMicrophone())
                {
                    ad_recordBtn.setVisibility(View.INVISIBLE);
                }

                ad_recordBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad_stopRecBtn.setVisibility(View.VISIBLE);
                        ad_playBtn.setVisibility(View.INVISIBLE);
                        ad_recordBtn.setVisibility(View.INVISIBLE);
                        ad_saveBtn.setEnabled(false);
                        ad_closeBtn.setEnabled(false);
                        recordAudio ();
                    }
                });

                ad_stopRecBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad_recordBtn.setVisibility(View.VISIBLE);
                        ad_stopRecBtn.setVisibility(View.INVISIBLE);
                        ad_playBtn.setVisibility(View.VISIBLE);
                        ad_pauseBtn.setVisibility(View.INVISIBLE);
                        ad_replayTV.setVisibility(View.VISIBLE);
                        ad_saveBtn.setEnabled(true);
                        ad_closeBtn.setEnabled(true);
                        stopRecording();
                    }
                });

                ad_playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ad_recordBtn.setVisibility(View.INVISIBLE);
                        ad_playBtn.setVisibility(View.INVISIBLE);
                        ad_pauseBtn.setVisibility(View.VISIBLE);
                        ad_saveBtn.setEnabled(false);
                        ad_closeBtn.setEnabled(false);
                        playAudio();
                    }
                });

                ad_pauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad_pauseBtn.setVisibility(View.INVISIBLE);
                        ad_playBtn.setVisibility(View.VISIBLE);
                        ad_recordBtn.setVisibility(View.VISIBLE);
                        ad_saveBtn.setEnabled(true);
                        ad_closeBtn.setEnabled(true);
                        pausedAudio();
                    }
                });
                mBuilder.setView(mView);

                final AlertDialog alertDialog = mBuilder.create();

                ad_closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (src!=start_src)
                        {
                            File file = new File (src);
                            file.delete();
                        }
                        alertDialog.dismiss();
                    }
                });

                ad_saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (src!=start_src) {
                            File file = new File(start_src);
                            file.delete();
                            mET_recordingSource.setText(src);
                        }
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });


        mS_isRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mS_isRecording.isChecked())
                {
                    mET_recordingSource.setVisibility(View.VISIBLE);
                    mB_toRecording.setVisibility(View.VISIBLE);
                }
                else{
                    mET_recordingSource.setVisibility(View.GONE);
                    mB_toRecording.setVisibility(View.GONE);
                }

            }
        });
        mS_isRecording.setChecked(false);
        mET_recordingSource.setVisibility(View.GONE);
        mB_toRecording.setVisibility(View.GONE);

        mET_date.setCursorVisible(false);
        mET_date.setFocusable(false);
        mET_date.setFocusableInTouchMode(false);
        mET_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        cDay = dayOfMonth;
                        cMonth = month;
                        cYear = year;
                        updateET_date();
                    }
                };
                new DatePickerDialog(v.getContext(), date, cYear, cMonth, cDay).show();
            }
        });


        mET_time.setCursorVisible(false);
        mET_time.setFocusable(false);
        mET_time.setFocusableInTouchMode(false);
        mET_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        cHour = hourOfDay;
                        cMinute = minute;
                        updateET_time();
                    }
                };
                new TimePickerDialog(v.getContext(), time, cHour, cMinute, true).show();
            }
        });

        mET_tags.setCursorVisible(false);
        mET_tags.setFocusable(false);
        mET_tags.setFocusableInTouchMode(false);
        mET_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoiceDialoge();
            }
        });


    }


    public void recordAudio ()
    {
        isRecording = true;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        //String tmp = AudioManager.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(src);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            Log.e("TMP", "prepare() failed");
        }


    }

    public void stopRecording ()
    {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
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



    private void showChoiceDialoge()
    {
        final String[] items = new String[listOfAllTags.size()];
        final boolean [] checkedStates = new boolean[listOfAllTags.size()];
        for (int i=0; i<listOfAllTags.size(); i++)
        {
            items[i] = listOfAllTags.get(i).getName();
            if (indexesOfChosenTags.contains(listOfAllTags.get(i).getId()))
                checkedStates[i] = true;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(mainWindowContext);
        builder.setTitle("Wybór tagów");
        builder.setMultiChoiceItems(items, checkedStates,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    }
                });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String outText= "";
                for(int x =0; x<checkedStates.length; x++)
                {
                    Integer IDtegoTagu = listOfAllTags.get(x).getId();
                    if(checkedStates[x])
                    {
                        if(!indexesOfChosenTags.contains(IDtegoTagu))
                        {
                            indexesOfChosenTags.add(IDtegoTagu);
                        }
                        outText = outText + items[x] + ", ";
                    }
                    else
                    {
                        if (indexesOfChosenTags.contains(IDtegoTagu))
                        {
                            indexesOfChosenTags.remove(indexesOfChosenTags.indexOf(IDtegoTagu));
                        }
                    }
                }

                if(outText.endsWith(", "))
                    outText = outText.substring(0, outText.length()-2);
                mET_tags.setText(outText);
            }
        });
        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Dodaj nowy tag", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String outText= "";
                for(int x =0; x<checkedStates.length; x++)
                {
                    Integer IDtegoTagu = listOfAllTags.get(x).getId();
                    if(checkedStates[x])
                    {
                        if(!indexesOfChosenTags.contains(IDtegoTagu))
                        {
                            indexesOfChosenTags.add(IDtegoTagu);
                        }
                        outText = outText + items[x] + ", ";
                    }
                    else
                    {
                        if (indexesOfChosenTags.contains(IDtegoTagu))
                        {
                            indexesOfChosenTags.remove(indexesOfChosenTags.indexOf(IDtegoTagu));
                        }
                    }
                }
                if(outText.endsWith(", "))
                    outText = outText.substring(0, outText.length()-2);
                mET_tags.setText(outText);

                dialog.dismiss();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(mainWindowContext);
                builder1.setTitle("Nowy tag");
                final EditText input = new EditText(mainWindowContext);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder1.setView(input);

                builder1.setPositiveButton("Dodaj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String output = input.getText().toString().toLowerCase().trim();
                        //TODO: tu ewentualnie sprawdzać czy już taki nie istniał
                        Tag newTag = new Tag(0,output);
                        mTagsViewModel.insertTag(newTag);

                        //TODO: zaznaczaj już od razu nowo-utworzony
                    }
                });
                builder1.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        showChoiceDialoge();
                    }
                });
                builder1.show();
            }
        });

        builder.show();
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
        getMenuInflater().inflate(R.menu.menu_add, menu);
        mojeMenu = menu;

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_save:
                String tmp = ""+((cDay<10)?"0"+cDay:cDay)+(" / ")+(((cMonth)<10)?"0"+(cMonth):(cMonth))+(" / ")+(cYear);
                String dateTimeString = tmp+" "+mET_time.getText();
                DateFormat format = new SimpleDateFormat("dd / MM / yyyy H : mm", Locale.ENGLISH);
                Date dateTimeDate = null;
                try {
                    dateTimeDate = format.parse(dateTimeString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String outDate = "";
                DateFormat format2 = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSSS", Locale.ENGLISH);
                if(dateTimeDate!=null)
                {
                    outDate = format2.format(dateTimeDate);
                }


                Dream resultDream = new Dream(
                        0,
                        ""+mET_dreamTitle.getText(),
                        mS_isRecording.isChecked()?1:0,
                        ""+mET_recordingSource.getText(),
                        ""+mET_dreamDesc.getText(),
                        ""+outDate);

                Intent replyIntent = new Intent();
                replyIntent.putExtra("NewDream", resultDream);
                replyIntent.putExtra("ListOfTags", indexesOfChosenTags);
                setResult(RESULT_OK, replyIntent);
                finish();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }






}
