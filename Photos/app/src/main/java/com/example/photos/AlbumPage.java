package com.example.photos;


import static android.content.ContentValues.TAG;

import static com.example.photos.HomeScreen.debug;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Displays an album in a grid of photo which can be selected and displayed in a slide show style along with operations, such as display, add, delete, copy, paste, move
 * @author Zihao Zheng
 */
public class AlbumPage extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    public static GridView gridView;
    Button add, delete, move, copy, display;

    private static final int PICK_IMAGE_REQUEST = 1;


    public Context mContext;

    public static Album currAlbum;

    /**
     * index of the clicked image
     */
    int index;

    public static boolean tempAlb = false;

    ArrayAdapter<String> adapter;


    /**
     * This method sets the data and click listeners when an activity is created
     * @param savedInstanceState    Bundle
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        mContext = this;
        setTitle(currAlbum.getAlbumName());

        // Initialize the buttons
        gridView = findViewById(R.id.GridView);
        add = findViewById(R.id.add);
        copy = findViewById(R.id.copy);
        display = findViewById(R.id.display);
        delete = findViewById(R.id.delete);
        move = findViewById(R.id.move);

        loadImages();


        // Bug Remover
//        for(int i=0;i<currAlbum.getPhotos().size();i++){
//            Photo photo = currAlbum.getPhotos().get(i);
//            if(photo.getName().equals("IMG_20230419_220611.jpg")||photo.getName().equals("IMG_20230419_220611.jpg")){
//                System.out.println(photo.getName());
//                currAlbum.removePhoto(photo);
//                try {
//                    HomeScreen.serialize();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }


        // Create the adapter


    }


    private void loadImages(){
        // Get the arraylist of urls to load in
        ArrayList<String> imageUrls = new ArrayList<>();
        for(Photo photo: currAlbum.getPhotos()){
            imageUrls.add("file://"+photo.getPath());
            if(debug){
                System.out.println("PATH-> "+photo.getPath());
            }
        }

        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                imageUrls
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                // Get the image URL for this position
                String imageUrl = getItem(position);

                // Create a new ImageView
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(new GridView.LayoutParams(
                        GridView.LayoutParams.MATCH_PARENT,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 100, getResources().getDisplayMetrics())
                ));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // Load the image into the ImageView using BitmapFactory
                try {
                    InputStream is = new URL(imageUrl).openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Set click listener for the image
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = getItem(position);

                        if(debug){
                            System.out.println(position+ " "+url.replace("file://", ""));
                        }

                        index = position;
                        if (index!= -1){
                            enableButtons(true);
                        }
                    }
                });

                // Return the ImageView
                return imageView;

            }
        };

        gridView.setAdapter(adapter);
        gridView.setNumColumns(3);
        gridView.setVerticalSpacing((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));



        // Check if we are in a temporary album (search results)
        if(tempAlb){
            hideButtons(true);
        }
    }




    /**
     * This method sets the visibility of the copy, move, display, and delete buttons based on the input parameter
     * @param status   boolean true -> visible, false -> invisible
     */
    private void enableButtons(boolean status){

        copy.setEnabled(status);
        move.setEnabled(status);
        display.setEnabled(status);
        delete.setEnabled(status);

        if(!status){
            index=-1;
        }
    }
    private void hideButtons(boolean status){
        copy.setVisibility(status ? View.INVISIBLE : View.VISIBLE);
        move.setVisibility(status ? View.INVISIBLE : View.VISIBLE);
        delete.setVisibility(status ? View.INVISIBLE : View.VISIBLE);
        add.setVisibility(status ? View.INVISIBLE : View.VISIBLE);
    }



    public void add(View view) {
        // Create an intent to pick an image
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Start the activity to pick an image
        startActivityForResult(intent, PICK_IMAGE_REQUEST);


    }

    // Handle the result of the image picker activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get the URI of the selected image
            Uri selectedImageUri = data.getData();

            // Get the file name of the selected image
            String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
            String fileName = null;
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                fileName = cursor.getString(columnIndex);
                cursor.close();
            }

            if (fileName != null) {
                // Check if the selected image already exists in the album
                for (Photo photo : currAlbum.getPhotos()) {
                    if (photo.getName().equals(fileName)) {
                        // Show a message that the image already exists
                        Toast.makeText(this, "Image already exists: " + fileName, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Check if the selected image already exists in other albums. if so, it would be like a COPY move
                for(Album album : Album.albums){
                    if(album.equals(currAlbum)){
                        continue;
                    }
                    for (Photo photo : album.getPhotos()) {
                        if (photo.getName().equals(fileName)) {
                            // Get the copy of the photo
                            Photo newPhoto = photo;

                            // Add the photo to the current album ()
                            currAlbum.addPhoto(newPhoto);

                            // update backend
                            try {
                                HomeScreen.serialize();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            // notify: Show a message that copy has been successfully done
                            Toast.makeText(this,  fileName+" copied from "+album.getAlbumName(), Toast.LENGTH_SHORT).show();

                            // reload page
                            loadImages();
                            return;
                        }
                    }
                }

                try {
                    // Copy the selected image to the internal storage with the same name
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                    byte[] buffer = new byte[1024];
                    int length;
                    // by default, overwrite if exist same file name
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    File file = new File(getFilesDir() + File.separator + fileName);

                    // Add the new photo to the current album
                    Photo newPhoto = new Photo(getFilesDir() + File.separator+fileName);
                    currAlbum.addPhoto(newPhoto);

                    HomeScreen.serialize();

                    // Show a message that the image was added
                    Toast.makeText(this, "Image added: " + fileName, Toast.LENGTH_LONG).show();
                    loadImages();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error: File not found!");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error: IO exception!");
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error: Security exception!");
                }
            }
        }
    }

    public void move(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("MOVE : Enter the name of destination:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

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
                        String destination = input.getText().toString().trim();
                        Album destAlb = null;
                        if (destination.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Destination name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // check if the destination album exists
                        for(Album album:Album.albums){
                            if(album.getAlbumName().equals(destination)){
                                destAlb = album;
                                break;
                            }
                        }
                        if(destAlb == null){
                            Toast.makeText(getApplicationContext(),"Album not found",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // check if the selected photo exist in the destination album
                        Photo currPhoto = currAlbum.getPhotos().get(index);
                        for(Photo photo:destAlb.getPhotos()){
                            if(photo.getName().equals(currPhoto.getName())){
                                Toast.makeText(getApplicationContext(),"Photo already exists in destination album",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        // give the destination album a copy of the photo
                        destAlb.addPhoto(currPhoto);
                        currAlbum.removePhoto(currPhoto);
                        Toast.makeText(getApplicationContext(), "Photo moved to "+destAlb.getAlbumName(), Toast.LENGTH_SHORT).show();

                        // Update the file
                        try {
                            HomeScreen.serialize();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        // unselect
                        enableButtons(false);
                        dialog.dismiss();

                        // reload
                        loadImages();
                    }
                });
            }
        });

        dialog.show();
    }
    public void copy(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("COPY : Enter the name of destination:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

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
                        String destination = input.getText().toString().trim();
                        Album destAlb = null;
                        if (destination.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Destination name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // check if the destination album exists
                        for(Album album:Album.albums){
                            if(album.getAlbumName().equals(destination)){
                                destAlb = album;
                                break;
                            }
                        }
                        if(destAlb == null){
                            Toast.makeText(getApplicationContext(),"Album not found",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // check if the selected photo exist in the destination album
                        Photo currPhoto = currAlbum.getPhotos().get(index);
                        for(Photo photo:destAlb.getPhotos()){
                            if(photo.getName().equals(currPhoto.getName())){
                                Toast.makeText(getApplicationContext(),"Photo already exists in destination album",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        // give the destination album a copy of the photo
                        destAlb.addPhoto(currPhoto);
                        Toast.makeText(getApplicationContext(), "Photo copied to "+destAlb.getAlbumName(), Toast.LENGTH_SHORT).show();

                        // Update the file
                        try {
                            HomeScreen.serialize();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        // unselect
                        enableButtons(false);
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }





    public void delete(View view) throws IOException {
        String fileName = currAlbum.getPhotos().get(index).getName();


        File file = new File(getFilesDir() + File.separator + fileName); // Create file object for current file name

        if (file.exists()) { // Check if file exists
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
                    for(Album album : Album.albums){
                        if(album.equals(currAlbum)){
                            continue;
                        }
                        for(Photo photo: album.getPhotos()){
                            if(photo.getName().equals(fileName)){
                                // There is another copy. just simply remove from this album, not entire storage
                                Toast.makeText(AlbumPage.this, fileName + " was deleted successfully from this album", Toast.LENGTH_SHORT).show();
                                currAlbum.removePhoto(currAlbum.getPhotos().get(index));
                                try {
                                    HomeScreen.serialize();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                enableButtons(false);
                                dialog.dismiss();

                                // reload
                                loadImages();
                                return;
                            }
                        }
                    }
                    boolean success = file.delete(); // Delete file
                    if (success) {
                        Toast.makeText(AlbumPage.this, fileName + " was deleted successfully.", Toast.LENGTH_SHORT).show();
                        currAlbum.removePhoto(currAlbum.getPhotos().get(index));
                        try {
                            HomeScreen.serialize();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        enableButtons(false);

                        //reload
                        loadImages();
                    } else {
                        Toast.makeText(AlbumPage.this, "ERROR!", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        } else {
            Toast.makeText(this, fileName + " does not exists", Toast.LENGTH_SHORT).show();        }
    }
    public void display(View view){
        if(currAlbum.getPhotos().size()<=0){
            enableButtons(false);
            return;
        }
        Display.currPhoto=currAlbum.getPhotos().get(index);
        Display.index= index;
        Display.currAlbum = currAlbum;

        Intent intent = new Intent(this, Display.class);
        startActivity(intent);
    }

    public void back(View view){
        tempAlb = false;
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }
}

