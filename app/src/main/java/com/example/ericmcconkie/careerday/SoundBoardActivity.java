package com.example.ericmcconkie.careerday;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.IOException;
import java.util.ArrayList;

public class SoundBoardActivity extends AppCompatActivity {
    public static  int ADDCOMPLETE = 999;

    private static String TAG = "SoundBoardActivity";

    private String[] animals = {"cow","duck","horse","rooster","tiger","turkey"};
    ArrayList<Animal> list = null;
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
        if(list == null){
            ArrayList<Animal> animalList = new ArrayList<Animal>();
            for(int i=0;i<animals.length;i++) {
                String animal = animals[i].toLowerCase();
                Animal a = new Animal();
                a.imageId = getResources().getIdentifier(animal,"drawable",this.getPackageName());
                a.soundId = getResources().getIdentifier(animal,"raw",this.getPackageName());

                animalList.add(a);
            }
            list = animalList;
        }

        //setup gridview
        final Context ctx = this;
        GridView gridview = (GridView) findViewById(R.id.gridview);
        ImageAdapter imageAdapter = new ImageAdapter(this,list);
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


                Animal newA = new Animal();
                newA.imagePath = image;
                newA.soundPath = sound;
                list.add(newA);

                update();
                Log.d(TAG, "onActivityResult: "+image);
            }
        }
    }
}
