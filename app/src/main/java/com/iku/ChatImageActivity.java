package com.iku;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatImageActivity extends AppCompatActivity {

    private StorageReference mStorageRef;
    private ImageView sendImageChatbtn;
    private Uri mImageUri, mainImageUri;
    private ImageView image;
    private EditText messageEntered;
    private ImageButton backButton;
    private int PICK_IMAGE = 1;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private FirebaseFirestore db;

    private int STORAGE_PERMISSION_CODE=10;

    private String TAG = ChatImageActivity.class.getSimpleName();

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        messageEntered = findViewById(R.id.messageTextField);
        mStorageRef = FirebaseStorage.getInstance().getReference("images");

        backButton = findViewById(R.id.backbutton);
        sendImageChatbtn = findViewById(R.id.sendMessageButton);
        image = findViewById(R.id.chosenImage);
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (ContextCompat.checkSelfPermission(ChatImageActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        } else {
            openFileChooser();
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        sendImageChatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!messageEntered.getText().toString().isEmpty()) {
                    try {
                        uploadFile(messageEntered.getText().toString());
                        sendImageChatbtn.setClickable(false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(ChatImageActivity.this, "Please add caption", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        if (uri != null) {
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cR.getType(uri));
        } else return null;
    }

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "images", null);

        if (path != null) return Uri.parse(path);
        else return null;
    }


    private void uploadFile(final String message) throws FileNotFoundException {
        if (mImageUri != null) {
            Bitmap imageSelected = decodeUri(this, mImageUri, 300);
            if (imageSelected != null)
                mainImageUri = getImageUri(this, imageSelected);
            if (mainImageUri != null) {
                final StorageReference imageRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mainImageUri));
                UploadTask uploadTask = imageRef.putFile(mainImageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> downloadUrl = imageRef.getDownloadUrl();
                    downloadUrl.addOnSuccessListener(uri -> {
                        Date d = new Date();
                        long timestamp = d.getTime();
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("message", message);
                        docData.put("timestamp", timestamp);
                        docData.put("uid", user.getUid());
                        docData.put("type", "image");
                        docData.put("imageUrl", uri.toString());
                        docData.put("userName", user.getDisplayName());
                        docData.put("upvoteCount", 0);
                        ArrayList<Object> upvotersArray = new ArrayList<>();
                        docData.put("upvoters", upvotersArray);

                        db.collection("iku_earth_messages")
                                .add(docData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        messageEntered.setText("");
                                        Toast.makeText(ChatImageActivity.this, "Image info uploaded", Toast.LENGTH_LONG).show();
                                        messageEntered.requestFocus();
                                        ChatImageActivity.super.onBackPressed();

                                        //log event
                                        Bundle params = new Bundle();
                                        params.putString("messaging", "image message");
                                        mFirebaseAnalytics.logEvent("image_sent", params);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    sendImageChatbtn.setClickable(true);
                                    Toast.makeText(ChatImageActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                });
                    });
                });
            }

        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_LONG).show();
        }
    }

    private void openFileChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            try {
                Bitmap imageSelected = decodeUri(this, mImageUri, 300);
                Uri tempMainImageUri = getImageUri(this, imageSelected);
                Log.i("ap", "Setting Image: " + tempMainImageUri);
                Picasso.get().load(tempMainImageUri).into(image);
                //image.setImageURI(tempMainImageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else
            onBackPressed();
    }

    private void requestStoragePermission() {

        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                openFileChooser();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}