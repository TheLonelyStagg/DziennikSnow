package com.example.thelonelystagg.dzienniksnow;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.thelonelystagg.dzienniksnow.database.DreamsViewModel;
import com.example.thelonelystagg.dzienniksnow.database.models.Dream;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AllDreamListAdapter.AllDreamListAdapterListener{

    static final int ADD_DREAM_REQUEST = 1;
    static final int EDIT_DREAM_REQUEST = 2;
    static final int SHOW_DREAM_REQUEST = 3;
    static final int DELETE_DREAM = 20;
    private int RECORD_AUDIO_REQUEST_CODE =123 ;


    private DreamsViewModel mDreamsViewModel;
    private AllDreamListAdapter adapter;
    private Menu mojeMenu;
    SearchView searchView;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_AUDIO_REQUEST_CODE);

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED){

                //Toast.makeText(this, "Record Audio permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        context = this;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new AllDreamListAdapter(this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDreamsViewModel = ViewModelProviders.of(this).get(DreamsViewModel.class);

        mDreamsViewModel.getAllDreams().observe(this, new Observer<List<Dream>>() {
            @Override
            public void onChanged(@Nullable List<Dream> dreams) {
                adapter.setDreams(dreams);
            }
        });



    }

    @Override
    public void onDreamSelected(Dream dream) {
        Intent intent = new Intent(context, ShowDreamActivity.class);
        intent.putExtra("NewDream", dream);
        ArrayList<Integer> indexesOfChosenTags = (ArrayList<Integer>) mDreamsViewModel.getTagIdsForDream(dream.getId());
        intent.putExtra("ListOfTags", indexesOfChosenTags);
        startActivityForResult(intent, SHOW_DREAM_REQUEST);
    }


    @Override
    public void onDreamSelectedLong(final Dream dream) {
        CharSequence colors[] = new CharSequence[]{"Edytuj", "Usuń"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Wybierz opcję");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(context, EditDreamActivity.class);
                    intent.putExtra("NewDream", dream);
                    ArrayList<Integer> indexesOfChosenTags = (ArrayList<Integer>) mDreamsViewModel.getTagIdsForDream(dream.getId());
                    intent.putExtra("ListOfTags", indexesOfChosenTags);
                    startActivityForResult(intent, EDIT_DREAM_REQUEST);
                }
                else
                {
                    mDreamsViewModel.deleteDream(dream);
                }
            }
        });
        builder.show();
    }


    //////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mojeMenu = menu;


        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.findItem(R.id.action_add).setVisible(false);
                menu.findItem(R.id.action_tags).setVisible(false);
            }
        });

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                menu.findItem(R.id.action_add).setVisible(true);
                menu.findItem(R.id.action_tags).setVisible(true);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                adapter.getFilter().filter(query);
                return false;
            }
        });

        return true;

    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {

            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_search:
                return true;

            case R.id.action_add:
                Intent intent = new Intent(this, AddDreamActivity.class);
                startActivityForResult(intent, ADD_DREAM_REQUEST);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ADD_DREAM_REQUEST:
                if(resultCode == RESULT_OK)
                {
                    Dream reply = (Dream) data.getSerializableExtra("NewDream");
                    List<Integer> repliedList = (ArrayList<Integer>) data.getSerializableExtra("ListOfTags");

                    mDreamsViewModel.insertDream(reply,repliedList);
                    mDreamsViewModel.cleanTagsTable();
                }
                break;
            case EDIT_DREAM_REQUEST:
                if(resultCode == RESULT_OK)
                {
                    Dream reply = (Dream) data.getSerializableExtra("NewDream");
                    List<Integer> repliedList = (ArrayList<Integer>) data.getSerializableExtra("ListOfTags");

                    mDreamsViewModel.updateDream(reply,repliedList);
                    mDreamsViewModel.cleanTagsTable();
                }
                break;
            case SHOW_DREAM_REQUEST:
                if(resultCode == RESULT_OK) //so edit
                {
                    Dream reply = (Dream) data.getSerializableExtra("NewDream");
                    ArrayList<Integer> repliedList = (ArrayList<Integer>) data.getSerializableExtra("ListOfTags");

                    Intent intent = new Intent(context, EditDreamActivity.class);
                    intent.putExtra("NewDream", reply);
                    intent.putExtra("ListOfTags", repliedList);
                    startActivityForResult(intent, EDIT_DREAM_REQUEST);
                }
                else if(resultCode == DELETE_DREAM)
                {
                    Dream reply = (Dream) data.getSerializableExtra("NewDream");
                    List<Integer> repliedList = (ArrayList<Integer>) data.getSerializableExtra("ListOfTags");

                    mDreamsViewModel.deleteDream(reply);
                    mDreamsViewModel.cleanTagsTable();
                }
                break;
        }



    }
}
