package com.example.ericmcconkie.careerday;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ericmcconkie.careerday.db.SQLiteDBHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddNewActivity extends AppCompatActivity {
    private static final String TAG = "AddNewActivity";
    private static final int CAMERA_REQUEST = 1888;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    private ImageView imageView;
    boolean mStartRecording = false;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private String timestamp = "";

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView)findViewById(R.id.imageView);

        //camera
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });


        //dictation
        // Record to the external cache directory for visibility
        Long tsLong = System.currentTimeMillis()/1000;
        timestamp = tsLong.toString();
        String abs = getExternalCacheDir().getAbsolutePath() + "/";
        if(getExternalMediaDirs().length > 0){
            abs = getExternalMediaDirs()[0].getAbsolutePath() + "/";
        }
        mFileName = abs + timestamp + "_sound.3gp";


        //check if we need permission to record
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }


        FloatingActionButton fabspeak = (FloatingActionButton) findViewById(R.id.fab_speak);
        fabspeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartRecording = !mStartRecording;
                if(mStartRecording){
                    startRecording();
                }else{
                    stopRecording();
                    startPlaying();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if(mFileName == null){
                Toast.makeText(this,"Please add a voice sample",Toast.LENGTH_SHORT).show();
                return false;
            }
            Bitmap bm=((BitmapDrawable)imageView.getDrawable()).getBitmap();
            String path = saveToInternalStorage(bm);
            if(path != null){
                //save and finsih
                //create a new animal
                Animal newA = new Animal();
                newA.imagePath = path;
                newA.soundPath = mFileName;

                //save to local storage
                SQLiteDBHelper.saveToDB(this,newA);

                //add data to the activity that called this..
                Intent resultIntent = new Intent();
                resultIntent.putExtra("image",path);
                resultIntent.putExtra("sound",mFileName);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //AUDIO PRIVATE METHODS
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    //Save image to disk
    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        String abs = getExternalCacheDir().getAbsolutePath() + "/";
        if(getExternalMediaDirs().length > 0){
            abs = getExternalMediaDirs()[0].getAbsolutePath() + "/";
        }

        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(abs,timestamp + "_image.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }/**/
        }
        return mypath.getPath();
    }

}
