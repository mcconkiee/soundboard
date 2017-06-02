package com.example.ericmcconkie.careerday;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.ericmcconkie.careerday.db.SQLiteDBHelper;

import java.io.IOException;
import java.util.ArrayList;

public class SoundBoardActivity extends AppCompatActivity {
    public static  int ADDCOMPLETE = 999;
    private static String TAG = "SoundBoardActivity";
    private String[] animals = {"cow","duck","horse","rooster","tiger","turkey"};
    ArrayList<Animal> list = null;
    public boolean selectMode = false;
    private ImageAdapter imageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton actionButton = (FloatingActionButton)findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addNewFragment = new Intent(SoundBoardActivity.this,AddNewActivity.class);
                SoundBoardActivity.this.startActivityForResult(addNewFragment,ADDCOMPLETE);
            }
        });
        update();
    }

    protected  void update(){
        //setup data
        ArrayList<Animal> animalList = new ArrayList<Animal>();
        for(int i=0;i<animals.length;i++) {
            String animal = animals[i].toLowerCase();
            Animal a = new Animal();
            a.imageId = getResources().getIdentifier(animal,"drawable",this.getPackageName());
            a.soundId = getResources().getIdentifier(animal,"raw",this.getPackageName());
            animalList.add(a);
        }
        list = animalList;

        //add any stored from our local db gets added last
        ArrayList<Animal> locals = SQLiteDBHelper.getAll(this);
        for(Animal a : locals){
            list.add(a);
        }

        //setup gridview
        final Context ctx = this;
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gridview.setMultiChoiceModeListener(new MultiChoiceModeListener());
        imageAdapter = new ImageAdapter(this,list);
        gridview.setAdapter(imageAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Animal animal = list.get(position);
                MediaPlayer mediaPlayer = null;
                if(animal.soundPath != null){
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(animal.soundPath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        Log.e(TAG, "prepare() failed");
                    }
                }else{
                    mediaPlayer = MediaPlayer.create(ctx,animal.soundId);
                }
                mediaPlayer.start();
            }
        });
    }

    private void smsSelectedItems(){
        ArrayList<Animal> list = imageAdapter.selected;
        if(list.size() >=1 ){
            Animal animal = list.get(0);
            try {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
//            sendIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
                sendIntent.putExtra("sms_body", "some text");
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + animal.imagePath));
                sendIntent.setType("image/jpeg");
                startActivity(sendIntent);;
            }catch (Exception e){
                Log.d(TAG, "smsSelectedItems: error"+ e);
            }

        }/**/
        update();
    }

    private void deleteSelectedItems(){
        ArrayList<Animal> list = imageAdapter.selected;
        for(Animal a : list){
            int done = SQLiteDBHelper.removeFromDB(this,a);
            boolean success = done > 0;
            Log.d(TAG, "deleteSelectedItems: "+ done);
        }
        update();
    }

    public class MultiChoiceModeListener implements
            GridView.MultiChoiceModeListener {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context, menu);
            selectMode = true;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.action_sms:
                    smsSelectedItems();
                    selectMode = false;
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_delete:
                    deleteSelectedItems();
                    selectMode = false;
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Here you can make any necessary updates to the activity when
            // the CAB is removed. By default, selected items are deselected/unchecked.
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ADDCOMPLETE){
            if (resultCode == Activity.RESULT_OK) {
                String image = data.getStringExtra("image");
                String sound = data.getStringExtra("sound");

                //create a new animal
                Animal newA = new Animal();
                newA.imagePath = image;
                newA.soundPath = sound;

                //save to local storage
                SQLiteDBHelper.saveToDB(this,newA);

                //update ui
                update();
            }
        }
    }
}
