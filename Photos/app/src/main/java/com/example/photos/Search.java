package com.example.photos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Search extends AppCompatActivity{
    /**
     * This method sets the data and click listeners when an activity is created
     * @param savedInstanceState    Bundle
     */

    public Context mContext;
    RadioButton PersonRadioButton ;
    RadioButton LocationRadioButton ;

    ListView searchResults;
    Button openSearch;
    EditText searchText;
    LinearLayout lin;
    Button search_button;
    EditText editText;

    public int targetTagIndex = -1;
    // Person or location depending on the radio button
    public String type;
    //The matching tags in original form, eg "person=sesh"
    public ArrayList<String> matches;
    // THe matching photos
    public ArrayList<Photo> searchResult;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = this;
        AlbumPage.tempAlb = false;

        initRadioButtons();

        // DONT FORGET THIS
        openSearch = findViewById(R.id.openSearch);
        lin = findViewById(R.id.lin);
        search_button = findViewById(R.id.search_button);
        searchText = findViewById(R.id.search_value_edit_text);
        searchResults = findViewById(R.id.searchResults);

        // Click listener for list view
        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the text of the clicked item
                String clickedItemName = parent.getItemAtPosition(position).toString();
                // Set the text of the button to "Open" + clickedItemName
                openSearch.setText("Open " + clickedItemName);
                targetTagIndex = position;
            }
        });

        // listener for Text field
        editText = findViewById(R.id.search_value_edit_text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    searchPhotos(null);
                    return true;
                }
                return false;
            }
        });




    }

    public void loadToList(){
        ArrayList<String> temp = new ArrayList<String>();
        for(String tag : matches){
            int index = tag.indexOf("=");
            String value = tag.substring(index + 1);
            temp.add(value);
        }
        // Pass the new ArrayList<String> to the ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, temp);
        searchResults.setAdapter(adapter);
        lin.setVisibility(View.VISIBLE);
        openSearch.setVisibility(View.VISIBLE);
    }

    public void initRadioButtons(){
        PersonRadioButton = findViewById(R.id.person_radio_button);
        LocationRadioButton = findViewById(R.id.location_radio_button);

        // set click listener for addPersonRadioButton
        PersonRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonRadioButton.setChecked(true);
                LocationRadioButton.setChecked(false);
            }
        });

        // set click listener for addLocationRadioButton
        LocationRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationRadioButton.setChecked(true);
                PersonRadioButton.setChecked(false);
            }
        });

    }
    public void backToHome(View view){
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
        AlbumPage.tempAlb = false;
    }

    public void searchPhotos(View view){
        // Check if empty
        String text = searchText.getText().toString().trim();
        String origText = text;
        if(text.equals("") || text == null || searchText.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Field cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // check if button is selected
        if(PersonRadioButton.isChecked()) {
            text = "person=" + text;
        } else if(LocationRadioButton.isChecked()) {
            text = "location=" + text;
        }else{
            Toast.makeText(getApplicationContext(), "Please select one of the radio buttons", Toast.LENGTH_SHORT).show();
            return;
        }
        // find all photos with the specific tag and place it into a arraylist
        searchResult = new ArrayList<Photo>();
        matches = new ArrayList<String>();
        type = (PersonRadioButton.isChecked()?"Person:":"Location:")+" ";
//        Album searchResult = new Album("Search by "+(PersonRadioButton.isChecked()?"Person:":"Location:")+" "+origText);
        for(Album album : Album.albums){
            for(Photo photo : album.getPhotos()){
                tagLoop:for(String tag : photo.getTags()){
                    // We have found the image!!
                    if(tag.toLowerCase().equals(text.toLowerCase())||tag.toLowerCase().contains(text.toLowerCase())){
                        // attempt to add the image into the result
                        for(Photo p : searchResult){
                            if(p.getName().equals(photo.getName())){
                                break tagLoop;
                            }
                        }
                        searchResult.add(photo);


                        for(String s : matches){
                            if(s.equals(tag)){
                                break tagLoop;
                            }
                        }
                        matches.add(tag);
                        break;
                    }
                }
            }
        }
        // check size of arraylist. if zero, toast message
        if(searchResult.size()<=0){
            Toast.makeText(getApplicationContext(), "No photos found under the tag", Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_button.getWindowToken(), 0);


        loadToList();
        Toast.makeText(getApplicationContext(), "Found "+matches.size()+ " Result(s)", Toast.LENGTH_SHORT).show();

        // display photos of this temporary album
//        AlbumPage.currAlbum = searchResult;
//        AlbumPage.tempAlb = true;

        // Switch scene/stage
//        Intent intent = new Intent(this, AlbumPage.class);
//        startActivity(intent);
    }

    public void openTempAlb(View view){
        if(targetTagIndex==-1){
            Toast.makeText(getApplicationContext(), "Please select a tag result", Toast.LENGTH_SHORT).show();
            return;
        }
        String origTag = matches.get(targetTagIndex);

        Album tempAlb = new Album("Search by "+type+ origTag.substring(origTag.indexOf("=") + 1));

        outerloop:for(Photo p : searchResult){
            for(String t : p.getTags()){
                if(t.equals(origTag)){
                    // check 重
                    for(Photo p1 :tempAlb.getPhotos()){
                        if(p1.getName().equals(p.getName())){
                            continue outerloop;
                        }
                    }
                    tempAlb.addPhoto(p);
                }
            }
        }


        // display photos of this temporary album
        AlbumPage.currAlbum = tempAlb;
        AlbumPage.tempAlb = true;

        // Switch scene/stage
        Intent intent = new Intent(this, AlbumPage.class);
        startActivity(intent);
    }

}

