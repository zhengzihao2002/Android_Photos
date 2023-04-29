package com.example.photos;


import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;

import com.example.photos.Photo;

import java.util.ArrayList;

/**
 * Allows the app to display photos in a GridView
 */

public class ImageAdapter extends BaseAdapter {
    private Context context;


    public static ArrayList<Photo> uris = new ArrayList<>();

    /**
     * This constructor sets the objects context to the app's context
     * @param c the context of the app
     */
    public ImageAdapter(Context c) {
        context = c;
    }

    /**
     * This app returns the size of the arraylist of photo objects
     * @return  int the size of the class's arraylist of photos
     */
    @Override
    public int getCount() {
        return uris.size();
    }

    /**
     * This method is never used, so it just returns null; however extending BaseAdapter requires it to be Overriden
     * @param index int the index of the item the user requests from the arraylist
     * @return null
     */
    @Override
    public Object getItem(int index) {
        return null;
    }

    /**
     * This method is never used, so it just returns 0; however extending BaseAdapter requires it to be Overriden
     * @param index int the id of the item the user requests from the arraylist
     * @return 0
     */
    @Override
    public long getItemId(int index) {
        return 0;
    }

    /**
     * This method sets the attributes of an ImageView
     * @param index int
     * @param convertView   View
     * @param parent    ViewGroup
     * @return ImageView with set attributes
     */
    public View getView(int index, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageURI(Uri.parse(uris.get(index).getName()));

        return imageView;
    }

    /**
     * This method gets the photo object at a specified index
     * @param index int which determines which spot in the the arraylist will be returned
     * @return
     */
    public Photo getPhoto(int index){
        return uris.get(index);
    }

    /**
     * This method adds a new photo object to the arraylist
     * @param add Uri of the photo the user would like to add to the arraylist
     */
    public void add(Uri add) {
//        uris.add(new Photo(add));
    }

    /**
     * This method adds a new photo object to the arraylist
     * @param add Photo object the user would like to add to the arraylist
     */
    public void add(Photo add) {
        uris.add(add);
    }

    /**
     * Removes photo at specific index from the arraylist
     * @param index int which specifies which spot will be removed
     */
    public void remove(int index) {
        uris.remove(index);
    }

    /**
     * This method gives back the arraylist of photos to the called
     * @return ArrayList<Photo> is the list of all photo objects in the album
     */
    public ArrayList<Photo> getPhotos() {
        return uris;
    }

    /**
     * This method clears the arraylist of all entries
     */
    public void clear() {
        uris.clear();
    }

}

