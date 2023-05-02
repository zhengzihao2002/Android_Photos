package com.example.photos;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
/**
 * Class for handling the home screen
 * @author Zihao Zheng
 */

public class HomeScreen extends AppCompatActivity implements Serializable {
    private static final long serialVersionUID = 1L;

    public static boolean debug=false;
    private ListView albumListView;
    private Button renameButton;
    private Button deleteButton;
    private Button openButton;
    private ArrayAdapter<String> albumAdapter;

    private Album selectedAlbum;
    public static File currDir;

    private boolean manualPickImage = false;
    private boolean manualDelete = false;
    private boolean manualCopyRawToStorage = false;
    private boolean setDefault = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        currDir = getFilesDir();

        if(setDefault){
            reset();
        }

        // Run OneTime Code
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("firstTimeAndroid16", true)) {
            // If the key not found,  Delete all files
            File dir = getFilesDir();
            if (dir.isDirectory()) {
                String[] children = dir.list();
                String[] fileNames = new String[children.length];
                for (int i = 0; i < children.length; i++) {
                    fileNames[i] = children[i];
                }
                manualDelete(fileNames);
            }

            // copy raw files into internal storage
            copyRawFilesToInternalStorage();
            Album.albums = new ArrayList<Album>();
            Album a = new Album("stock");
            a.addPhoto(new Photo(getFilesDir()+File.separator+"stock1.jpg"));
            a.addPhoto(new Photo(getFilesDir()+File.separator+"stock2.jpg"));
            a.addPhoto(new Photo(getFilesDir()+File.separator+"stock3.jpg"));
            a.addPhoto(new Photo(getFilesDir()+File.separator+"stock4.jpg"));
            Album.albums.add(a);
            // Serialize the fresh data
            try {
                serialize();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // mark first time has ran.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTimeAndroid16", false);
            editor.commit();
        }






        // Developer Controls
        if(manualPickImage){
            Toast.makeText(this, "Disabled at this time", Toast.LENGTH_SHORT).show();
//            pickImage();
        }
        if(manualDelete){
            String[] myArray = {};
            manualDelete(myArray);
        }
        if(manualCopyRawToStorage){
            copyRawFilesToInternalStorage();
        }




        // Temporarily print out all the files from this unknown directory
        printAllFiles();

        // Initialize the albums (load from file)
        try {
//            manualModifyData();
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Initialize the List View Object
        albumListView = findViewById(R.id.albums_list);
        renameButton = findViewById(R.id.rename_button);
        deleteButton = findViewById(R.id.delete_button);
        openButton = findViewById(R.id.open_button);
        // Give the List View Object a listener that shows it is clicked
        albumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get the selected item name
                String selectedItem = parent.getItemAtPosition(position).toString();
                // make the buttons visible
                setButtonVisible(true);

                // set the green background tint for the selected item
//                AnimationDrawable animDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.bg_green_anim);
//                view.setBackground(animDrawable);
//                animDrawable.start();

//                view.setBackgroundResource(android.R.color.holo_green_light);
//                view.setBackgroundResource(R.color.qq_yellow);

                // remove the tint from other items
//                for(int i=0; i<parent.getChildCount(); i++) {
//                    if(i != position) {
//                        parent.getChildAt(i).setBackground(null);
//                    } else {
//                        animDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.bg_green_anim);
//                        animDrawable.stop();
//                    }
//                }

//                for(int i=0; i<parent.getChildCount(); i++) {
//                    if(i != position) {
//                        parent.getChildAt(i).setBackground(null);
//                    }
//                }

                setTitle("Selected Album: "+selectedItem);

                // Set the (possible) current album to be the clicked album
                AlbumPage.currAlbum=Album.albums.get(position);
                selectedAlbum = Album.albums.get(position);
            }
        });



        // Get the albums
        ArrayList<String> albumNames = getAlbumNamesFromPreviousSession();

        // Create adapter with album names
        albumAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumNames);

        // Set adapter to ListView
        albumListView.setAdapter(albumAdapter);

    }
    public void reset(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstTimeAndroid16", true);
        editor.commit();
    }


    // Helper method to get album names from previous session
    private ArrayList<String> getAlbumNamesFromPreviousSession() {
        // Implement this method to retrieve album names from previous session
        // or return an empty ArrayList if there is no previous session

        if(Album.albums == null){
            Album.albums = new ArrayList<Album>();
        }
        ArrayList<Album> albums = Album.albums;
        ArrayList<String> result = new ArrayList<String>();
        for(Album album:albums){
            result.add(album.getAlbumName());
        }

        return result;

    }

    private void manualModifyData() throws IOException {
        ArrayList<Album> albums = new ArrayList<Album>();

        Album a1 = new Album("Album1");
        a1.addPhoto(new Photo(getFilesDir() + File.separator + "my_photo.jpg"));
        a1.addPhoto(new Photo(getFilesDir() + File.separator + "stock1.jpg"));
        a1.addPhoto(new Photo(getFilesDir() + File.separator + "stock2.jpg"));
        a1.addPhoto(new Photo(getFilesDir() + File.separator + "stock3.jpg"));
        a1.addPhoto(new Photo(getFilesDir() + File.separator + "stock4.jpg"));
        albums.add(a1);


        albums.add(new Album("Album2"));
        albums.add(new Album("Album3"));
        albums.add(new Album("Album4"));
        albums.add(new Album("Album5"));
        albums.add(new Album("Album6"));
        albums.add(new Album("Album7"));
        albums.add(new Album("Album8"));
        albums.add(new Album("Album9"));



        Album.albums=albums;
        serialize();
    }

    /**
     * The method for initializing the Privacy class. it deserializes the data from file so the session before is loaded
     * @throws IOException a exception if the deserialize is failed
     * */
    public void init() throws IOException, ClassNotFoundException {
        // Get the path to the app's private directory
        File dir = getFilesDir();
        // Create a File object for the target file
        File file = new File(dir, "albumList.ser");
        // Check if the file exists
        if (file.exists()) {
            // If the file exists, display a Toast message indicating that it exists
            // albumList.ser exists
            Toast.makeText(this, "Launch Successful", Toast.LENGTH_SHORT).show();
        } else {
            // If the file doesn't exist, create it and display a Toast message indicating that it was created
            try {
                file.createNewFile();
                if(debug){
                    System.out.println(file.getAbsolutePath());
                }
                // albumList.ser created
                Toast.makeText(this, "Initial Launch Successful", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // retrieve the data from the file and insert it into the array list
        deserialize();
    }
    /**
     * The method for serializing the all the users and its data into the data file.
     *  @throws IOException a exception if the serialize is failed
     */
    public static void serialize() throws IOException {
        File file = new File(currDir, "albumList.ser");
        if(!file.exists()){
            if(debug){
                System.out.println("storage file created!!");
            }
            file.createNewFile();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(Album.albums);
            oos.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    /**
     * The method for deserializing the all the users and its data out of the data file.
     *  @throws IOException a exception if the deserialize is failed
     */
    public static void deserialize() throws IOException, ClassNotFoundException {
        File file = new File(currDir, "albumList.ser");
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<Album> deserializedAlbumList = (ArrayList<Album>) ois.readObject();
            ois.close();
            fis.close();
            // do something with the deserializedAlbumList
            Album.albums=deserializedAlbumList;

            if(Album.albums == null){
                Album.albums = new ArrayList<Album>();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void saveFile(String file, String text){
        try {
            FileOutputStream fos = openFileOutput(file,Context.MODE_PRIVATE);
            fos.write(text.getBytes());
            fos.close();
            Toast.makeText(HomeScreen.this,"Saved!",Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public String readFile(String file){
        String text="";
        try {
            FileInputStream fis = openFileInput(file);
            int size = fis.available();
            byte [] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            text=new String(buffer);
            Toast.makeText(HomeScreen.this,"Saved!",Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            e.printStackTrace();
        }
        return text;
    }


    public void printAllFiles(){
        // Get the path to the app's private directory
        File dir = getFilesDir();

        // List all the files in the directory
        File[] files = dir.listFiles();

        // Create a string to hold the file names

        // Loop through the files and add their names to the string
        if(debug){
            for (File file : files) {
                System.out.println(getFilesDir()+File.separator+file.getName());
            }
        }
    }



    public void search(View view) {
        Intent intent = new Intent(this, Search.class);
        startActivity(intent);
    }
    public void create(View view) {
        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the name of the new album");

        // Create the text field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Create the two buttons
        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Render the Dialog
        final AlertDialog dialog = builder.create();

        // Override the onClickListener for the positive button
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Check if the destination is empty
                        if(input.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(), "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String destination = input.getText().toString().trim();
                        if(destination.equals("")||destination==null){
                            Toast.makeText(getApplicationContext(), "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // check if the destination album already exists
                        for(Album album : Album.albums){
                            if(album.getAlbumName().equals(destination)){
                                Toast.makeText(getApplicationContext(), "Album already exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        // Add the album to the global arraylist
                        Album.albums.add(new Album(destination));
                        Toast.makeText(getApplicationContext(), "Successfully created: "+destination, Toast.LENGTH_SHORT).show();

                        // reload page
                        ArrayList<String> albumNames = getAlbumNamesFromPreviousSession();
                        albumAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, albumNames);
                        albumListView.setAdapter(albumAdapter);

                        // Update the file
                        try {
                            serialize();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }
    public void rename(View view) {
        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the name of the new album");

        // Create the text field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(selectedAlbum.getAlbumName());
        builder.setView(input);

        // Create the two buttons
        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Render the Dialog
        final AlertDialog dialog = builder.create();

        // Override the onClickListener for the positive button
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Check if the destination is empty
                        if(input.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(), "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String destination = input.getText().toString().trim();
                        if(destination.equals("")||destination==null){
                            Toast.makeText(getApplicationContext(), "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // If we change nothing
                        if(destination.equals(selectedAlbum.getAlbumName())){
                            Toast.makeText(getApplicationContext(), "No changes made", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        // check if the destination album already exists
                        for(Album album : Album.albums){
                            if(album.getAlbumName().equals(destination)){
                                Toast.makeText(getApplicationContext(), "Album already exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        // change the album name in the global arraylist
                        selectedAlbum.setAlbumName(destination);
                        Toast.makeText(getApplicationContext(), "Successfully renamed: "+destination, Toast.LENGTH_SHORT).show();

                        // reload page
                        ArrayList<String> albumNames = getAlbumNamesFromPreviousSession();
                        albumAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, albumNames);
                        albumListView.setAdapter(albumAdapter);

                        // Update the file
                        try {
                            serialize();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        dialog.dismiss();
                        setTitle("Selected Album: "+destination);
                    }
                });
            }
        });

        dialog.show();
    }
    public void delete(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.delete_confirmation_dialog, null);
        builder.setView(dialogView);

        TextView message = dialogView.findViewById(R.id.dialog_message);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Sucessfully Deleted: "+selectedAlbum.getAlbumName(), Toast.LENGTH_SHORT).show();
                Album.albums.remove(selectedAlbum);
                dialog.dismiss();

                // reload
                try {
                    serialize();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // Get the albums
                ArrayList<String> albumNames = getAlbumNamesFromPreviousSession();

                // Create adapter with album names
                albumAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, albumNames);

                // Set adapter to ListView
                albumListView.setAdapter(albumAdapter);

                // hide select only buttons
                setButtonVisible(false);

                // Update title
                setTitle("Photos");

                // Remove extraneous photos
                removeExtraneous();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    public void setButtonVisible(boolean visibility){
        renameButton.setVisibility(visibility? View.VISIBLE:View.INVISIBLE);
        deleteButton.setVisibility(visibility? View.VISIBLE:View.INVISIBLE);
        openButton.setVisibility(visibility? View.VISIBLE:View.INVISIBLE);
    }
    public void removeExtraneous(){
        // Get the path to the app's private directory
        File dir = getFilesDir();
        // List all the files in the directory
        File[] files = dir.listFiles();
        nextPhoto:for(File file : files){
            if(file.getName().equals("albumList.ser")){
                // DO NOT delete the serialization data file
                continue;
            }
            // Look through each album to find if it contains this photo
            for(Album album : Album.albums){
                for(Photo photo: album.getPhotos()){
                    if(photo.getName().equals(file.getName())){
                        continue nextPhoto;
                    }
                }
            }
            file.delete();
            if(debug){
                System.out.println("EXTRANEOUS DELETED-> "+file.getName());
            }
        }
    }
    public void open(View view) {
        Intent intent = new Intent(this, AlbumPage.class);
        startActivity(intent);
        AlbumPage.tempAlb=false;
    }

    public void manualDelete(String[] myArray) {

        String currentDir = System.getProperty("user.dir"); // Get current directory

        for (String fileName : myArray) {
            File file = new File(getFilesDir() + File.separator + fileName); // Create file object for current file name
            if (file.exists()) { // Check if file exists
                boolean success = file.delete(); // Delete file
                if (success) {
                    if(debug){
                        System.out.println(fileName + " was deleted successfully.");
                    }
                } else {
                    if(debug){
                        System.out.println("Error deleting " + fileName);
                    }
                }
            } else {
                if(debug){
                    System.out.println(fileName + " does not exist.");
                }
            }
        }
    }

    private void copyRawFilesToInternalStorage() {
        InputStream inputStream;
        FileOutputStream outputStream;

        try {
            // Get the list of files in the res/raw folder
            Field[] fields = R.raw.class.getFields();

            // Copy each file to internal storage if it doesn't exist already
            for (Field field : fields) {
                String filename = field.getName();
                if (!fileExistsInInternalStorage(filename)) {
                    // Open the input and output streams
                    inputStream = getResources().openRawResource(
                            getResources().getIdentifier(filename, "raw", getPackageName()));
                    outputStream = openFileOutput(filename+".jpg", Context.MODE_PRIVATE);

                    // Copy the file
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    // Close the streams
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private boolean fileExistsInInternalStorage(String filename) {
        File file = new File(getFilesDir(), filename);
        return file.exists();
    }


}