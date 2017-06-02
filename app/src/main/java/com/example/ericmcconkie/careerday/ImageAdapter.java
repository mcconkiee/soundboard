package com.example.ericmcconkie.careerday;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by ericmcconkie on 6/1/17.
 */

public class ImageAdapter extends BaseAdapter {
    private static String TAG = "ImageAdapter";
    private Context mContext;
    private ArrayList<Animal> mData;

    public ImageAdapter(Context c, ArrayList<Animal> data) {
        mContext = c;
        mData = data;

    }

    public int getCount() {
        if(mData != null){
            return mData.size();
        }
        return 0;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
//            imageView.setLayoutParams(new GridView.LayoutParams(wt,ht));
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(8, 8, 8, 8);

        } else {
            imageView = (ImageView) convertView;
        }
        Animal animal = mData.get(position);
        if(animal.imagePath != null){
            Uri uri = Uri.fromFile(new File(animal.imagePath));
            imageView.setImageURI(uri);
        }else{
            imageView.setImageResource(animal.imageId);
        }

        return imageView;
    }


}
