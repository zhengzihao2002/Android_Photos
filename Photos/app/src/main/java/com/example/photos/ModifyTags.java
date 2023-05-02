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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
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
 * Modifies the tag by adding or deleteing
 * @author Zihao Zheng
 */
public class ModifyTags extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;

    public Context mContext;

    public static Photo currPhoto;

    public EditText add_tag;
    public EditText delete_tag;

    RadioButton addPersonRadioButton ;
    RadioButton addLocationRadioButton ;
    RadioButton deletePersonRadioButton ;
    RadioButton deleteLocationRadioButton ;


    /**
     * This method sets the data and click listeners when an activity is created
     * @param savedInstanceState    Bundle
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifytags);
        mContext = this;
        setTitle(currPhoto.getName());

        init_add_radio_buttons();
        init_delete_radio_buttons();

        add_tag = findViewById(R.id.tag_value_edittext);
        delete_tag = findViewById(R.id.tag_value_edittext2);

    }

    public void init_add_radio_buttons(){
        addPersonRadioButton = findViewById(R.id.add_person);
        addLocationRadioButton = findViewById(R.id.add_location);

        // set click listener for addPersonRadioButton
        addPersonRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPersonRadioButton.setChecked(true);
                addLocationRadioButton.setChecked(false);
            }
        });

        // set click listener for addLocationRadioButton
        addLocationRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocationRadioButton.setChecked(true);
                addPersonRadioButton.setChecked(false);
            }
        });

    }
    public void init_delete_radio_buttons(){
        deletePersonRadioButton = findViewById(R.id.delete_person);
        deleteLocationRadioButton = findViewById(R.id.delete_location);

        // set click listener for addPersonRadioButton
        deletePersonRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePersonRadioButton.setChecked(true);
                deleteLocationRadioButton.setChecked(false);
            }
        });

        // set click listener for addLocationRadioButton
        deleteLocationRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLocationRadioButton.setChecked(true);
                deletePersonRadioButton.setChecked(false);
            }
        });

    }

    public void deleteTag(View view) throws IOException {
        // check if empty
        if(delete_tag.getText().toString().trim().isEmpty()){
            Toast.makeText(getApplicationContext(), "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the tag value
        String text = delete_tag.getText().toString().trim();
        if(text.equals("")||text==null){
            Toast.makeText(getApplicationContext(), "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (text.contains("=")) {
            Toast.makeText(getApplicationContext(), "Tag cannot contain equal sign (=)", Toast.LENGTH_SHORT).show();
            return;
        }
        if(deletePersonRadioButton.isChecked()) {
            text = "person=" + text;
        } else if(deleteLocationRadioButton.isChecked()) {
            text = "location=" + text;
        }else{
            Toast.makeText(getApplicationContext(), "Please select one of the radio buttons", Toast.LENGTH_SHORT).show();
            return;
        }
        // check if the tag exists
        boolean status = false;
        for(String tag : currPhoto.getTags()){
            if(tag.equals(text)){
                status = true;
            }
        }
        if(!status){
            Toast.makeText(getApplicationContext(), "Tag does not exists", Toast.LENGTH_SHORT).show();
            return;
        }
        // delete the tag
        currPhoto.removeTag(text);
        Toast.makeText(getApplicationContext(), "Successful!", Toast.LENGTH_SHORT).show();
        // update file (serialize)
        HomeScreen.serialize();
        // Go back
        Intent intent = new Intent(this, Display.class);
        startActivity(intent);
    }
    public void addTag(View view) throws IOException {
        // check if empty
        if(add_tag.getText().toString().trim().isEmpty()){
            Toast.makeText(getApplicationContext(), "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the tag value
        String text = add_tag.getText().toString().trim();
        if(text.equals("")||text==null){
            Toast.makeText(getApplicationContext(), "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (text.contains("=")) {
            Toast.makeText(getApplicationContext(), "Tag cannot contain equal sign (=)", Toast.LENGTH_SHORT).show();
            return;
        }
        if(addPersonRadioButton.isChecked()) {
            text = "person=" + text;
        } else if(addLocationRadioButton.isChecked()) {
            text = "location=" + text;
        }else{
            Toast.makeText(getApplicationContext(), "Please select one of the radio buttons", Toast.LENGTH_SHORT).show();
            return;
        }
        // check if the tag exists
        for(String tag : currPhoto.getTags()){
            if(tag.equals(text)){
                Toast.makeText(getApplicationContext(), "Tag Already Exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // add the tag
        currPhoto.addTag(text);
        Toast.makeText(getApplicationContext(), "Successful!", Toast.LENGTH_SHORT).show();
        // update file (serialize)
        HomeScreen.serialize();
        // Go back
        Intent intent = new Intent(this, Display.class);
        startActivity(intent);
    }

    public void backToPhoto(View view){
        Intent intent = new Intent(this, Display.class);
        startActivity(intent);
    }



}
