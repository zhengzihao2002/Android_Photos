package com.example.photos;

import static com.example.photos.AlbumPage.currAlbum;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
/**
 * Displays a image seperately and have slide show functionality
 * @author Zihao Zheng
 */
public class Display extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;

    public static ArrayAdapter<String> adapter;

    public Context mContext;

    public static Photo currPhoto;

    public ImageView imageView;

    public TextView listView;

    public static int index;
    public static Album currAlbum;

    /**
     * This method sets the data and click listeners when an activity is created
     * @param savedInstanceState    Bundle
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        mContext = this;
        setTitle(currAlbum.getAlbumName());

        // load the image
        imageView=findViewById(R.id.imageView);
        if(currPhoto!=null){
            imageView.setImageURI(Uri.parse(currPhoto.getPath()));
        }

        // load the tags
        listView = findViewById(R.id.tags_list_view);
        ArrayList<String> tags = currPhoto.getTags();
        StringBuilder tagsString = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            tagsString.append(tags.get(i));
            if (i < tags.size() - 1) {
                tagsString.append(", ");
            }
        }
        listView.setText(tagsString.toString());
        listView.setMovementMethod(new ScrollingMovementMethod());





    }


    public void prevPhoto(View view){
        // update index
        index--;
        if(index < 0 ){
            // If negative, go back to last photo
            index = currAlbum.getPhotos().size()-1;
        }

        // update photo
        currPhoto = currAlbum.getPhotos().get(index);
        imageView.setImageURI(Uri.parse(currPhoto.getPath()));

        // load the tags
        listView = findViewById(R.id.tags_list_view);
        ArrayList<String> tags = currPhoto.getTags();
        StringBuilder tagsString = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            tagsString.append(tags.get(i));
            if (i < tags.size() - 1) {
                tagsString.append(", ");
            }
        }
        listView.setText(tagsString.toString());
        listView.setMovementMethod(new ScrollingMovementMethod());
    }
    public void nextPhoto(View view){
        // update index
        index++;
        if(index >=  currAlbum.getPhotos().size()){
            // If negative, go back to last photo
            index = 0;
        }

        // update photo
        currPhoto = currAlbum.getPhotos().get(index);
        imageView.setImageURI(Uri.parse(currPhoto.getPath()));

        // load the tags
        listView = findViewById(R.id.tags_list_view);
        ArrayList<String> tags = currPhoto.getTags();
        StringBuilder tagsString = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            tagsString.append(tags.get(i));
            if (i < tags.size() - 1) {
                tagsString.append(", ");
            }
        }
        listView.setText(tagsString.toString());
        listView.setMovementMethod(new ScrollingMovementMethod());
    }


    public void backToAlbum(View view){
        Intent intent = new Intent(this, AlbumPage.class);
        startActivity(intent);
        currPhoto=null;
    }

    public void modifyTags(View view){
//ERROR        Caused by: android.content.ActivityNotFoundException: Unable to find explicit activity class {com.example.photos/com.example.photos.ModifyTags}; have you declared this activity in your AndroidManifest.xml?
//SOLVE        Declare in manifest
                ModifyTags.currPhoto=currPhoto;
        Intent intent = new Intent(this, ModifyTags.class);
        startActivity(intent);
    }

}
