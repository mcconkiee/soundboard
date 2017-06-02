package com.example.ericmcconkie.careerday.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.ericmcconkie.careerday.Animal;

import java.util.ArrayList;

/**
 * Created by ericmcconkie on 6/2/17.
 * http://www.androidauthority.com/how-to-store-data-locally-in-android-app-717190/
 */

public class SQLiteDBHelper extends SQLiteOpenHelper{
    private  static String TAG = "SQLiteDBHelper";
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "soundboard_animals";
    public static final String ANIMAL_TABLE_NAME = "animals";
    public static final String ANIMAL_IMAGE = "animal_image";
    public static final String ANIMAL_SOUND = "animal_sound";
    public static final String ANIMAL_ID = "_id";


    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + ANIMAL_TABLE_NAME + " (" +
                ANIMAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ANIMAL_SOUND + " TEXT, " +
                ANIMAL_IMAGE + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ANIMAL_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public static int removeFromDB(Context ctx, Animal animal) {
        SQLiteDatabase database = new SQLiteDBHelper(ctx).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteDBHelper.ANIMAL_IMAGE, animal.imagePath);
        values.put(SQLiteDBHelper.ANIMAL_SOUND, animal.soundPath);

        String query = ANIMAL_ID + "=" + Integer.toString(animal.animalId);
        Log.d(TAG, "removeFromDB: "+ query);
        return database.delete(ANIMAL_TABLE_NAME, query, null) ;
    }

    public static  long saveToDB(Context ctx, Animal animal) {
        SQLiteDatabase database = new SQLiteDBHelper(ctx).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteDBHelper.ANIMAL_IMAGE, animal.imagePath);
        values.put(SQLiteDBHelper.ANIMAL_SOUND, animal.soundPath);
        long newRowId = database.insert(SQLiteDBHelper.ANIMAL_TABLE_NAME, null, values);

        return newRowId;
    }

    public static ArrayList<Animal> getAll(Context ctx) {
        SQLiteDatabase database = new SQLiteDBHelper(ctx).getReadableDatabase();

        String[] projection = {
                SQLiteDBHelper.ANIMAL_ID,
                SQLiteDBHelper.ANIMAL_IMAGE,
                SQLiteDBHelper.ANIMAL_SOUND
        };

        Cursor cursor = database.query(
                SQLiteDBHelper.ANIMAL_TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // don't sort
        );

        Log.d("TAG", "The total cursor count is " + cursor.getCount());

        ArrayList<Animal> list = new ArrayList<Animal>();
        if (cursor.moveToFirst()){
            do{
                String imagePath    = cursor.getString(cursor.getColumnIndex(ANIMAL_IMAGE));
                String soundPath    = cursor.getString(cursor.getColumnIndex(ANIMAL_SOUND));
                String animalId     = cursor.getString(cursor.getColumnIndex(ANIMAL_ID));
                // do what ever you want here
                Animal a = new Animal();
                a.imagePath = imagePath;
                a.soundPath = soundPath;
                a.animalId = Integer.parseInt(animalId);
                list.add(a);
            }while(cursor.moveToNext());
        }
        cursor.close();

        return list;

    }
    
    
}
