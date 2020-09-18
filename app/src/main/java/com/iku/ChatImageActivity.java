package com.iku;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatImageActivity extends AppCompatActivity {

    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAnalytics mFirebaseAnalytics;
    private SimpleDateFormat dateFormatter;
    private Uri mImageUri, finalUri;
    private int PICK_IMAGE = 1;
    private String docId, message, imageUrl;

    private int STORAGE_PERMISSION_CODE = 10;

    private String[] appPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private String TAG = ChatImageActivity.class.getSimpleName();

    private ImageButton sendImageChatbtn;

    private ImageView backButton;

    private PhotoView chosenImage;

    private EditText messageEntered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mStorageRef = FirebaseStorage.getInstance().getReference(user.getUid());
        db = FirebaseFirestore.getInstance();
        messageEntered = findViewById(R.id.messageTextField);
        backButton = findViewById(R.id.backbutton);
        sendImageChatbtn = findViewById(R.id.sendMessageButton);
        chosenImage = findViewById(R.id.chosenImage);
        dateFormatter = new SimpleDateFormat(
                DATE_FORMAT, Locale.US);


        Bundle extras = getIntent().getExtras();
        docId = extras.getString("documentId");
        if (docId.equals("default")) {
            openFileChooser();
        } else if (!docId.equals("default")) {
            message = extras.getString("message");
            messageEntered.setText(message);
            imageUrl = extras.getString("imageUrl");
            Picasso.get().load(imageUrl).into(chosenImage);
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

                    if (docId.equals("default")) {

                        uploadFile(messageEntered.getText().toString());

                        sendImageChatbtn.setClickable(false);

                    } else if (!docId.equals("default")) {
                        updateMessage(docId, messageEntered.getText().toString());
                    }

                } else
                    Toast.makeText(ChatImageActivity.this, "Caption such empty..much wow!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadFile(String message) {
        if (finalUri != null) {
            StorageReference imageRef = mStorageRef.child("IKU-img_"
                    + dateFormatter.format(new Date()) + ".png");
            UploadTask uploadTask = imageRef.putFile(finalUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Task<Uri> downloadUrl = imageRef.getDownloadUrl();
                downloadUrl.addOnSuccessListener(uri -> {
                    Date d = new Date();
                    long timestamp = d.getTime();
                    Map<String, Object> docData = new HashMap<>();
                    docData.put("message", message.trim());
                    docData.put("timestamp", timestamp);
                    docData.put("uid", user.getUid());
                    docData.put("type", "image");
                    docData.put("imageUrl", uri.toString());
                    docData.put("userName", user.getDisplayName());
                    docData.put("upvoteCount", 0);
                    ArrayList<Object> upvotersArray = new ArrayList<>();
                    docData.put("upvoters", upvotersArray);
                    ArrayList<Object> thumbsUpArray = new ArrayList<>();
                    docData.put("emoji1", thumbsUpArray);
                    ArrayList<Object> clapsArray = new ArrayList<>();
                    docData.put("emoji2", clapsArray);
                    ArrayList<Object> thinkArray = new ArrayList<>();
                    docData.put("emoji3", thinkArray);
                    ArrayList<Object> ideaArray = new ArrayList<>();
                    docData.put("emoji4", ideaArray);
                    ArrayList<Object> dounvotersArray = new ArrayList<>();
                    docData.put("downvoters", dounvotersArray);
                    docData.put("downvoteCount", 0);
                    docData.put("edited", false);

                    Map<String, Object> normalMessage = new HashMap<>();
                    normalMessage.put("firstImage", true);

                    db.collection("iku_earth_messages")
                            .add(docData)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    db.collection("users").document(user.getUid()).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            Boolean isFirstImage = (Boolean) document.get("firstImage");
                                                            if (!isFirstImage) {
                                                                db.collection("users").document(user.getUid())
                                                                        .update(normalMessage)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                //Log event
                                                                                Bundle params = new Bundle();
                                                                                params.putString("type", "image");
                                                                                params.putString("uid", user.getUid());
                                                                                mFirebaseAnalytics.logEvent("first_message", params);
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {

                                                                            }
                                                                        });

                                                            }
                                                        } else {
                                                        }
                                                    } else {
                                                    }
                                                }
                                            });


                                    messageEntered.setText("");
                                    Toast.makeText(ChatImageActivity.this, "Aren't you the best", Toast.LENGTH_LONG).show();
                                    messageEntered.requestFocus();
                                    ChatImageActivity.super.onBackPressed();

                                    //Log event
                                    Bundle params = new Bundle();
                                    params.putString("type", "image");
                                    params.putString("uid", user.getUid());
                                    mFirebaseAnalytics.logEvent("messaging", params);
                                }
                            })
                            .addOnFailureListener(e -> {
                                sendImageChatbtn.setClickable(true);
                            });
                });
            });
        } else
            onBackPressed();
    }

    private void openFileChooser() {
        if (checkAndRequestPermissions()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            mImageUri = data.getData();
            try {
                Bitmap bitmap = getThumbnail(mImageUri);
                Log.i(TAG, "onActivityResult: " + bitmap.getWidth() + "\n" + bitmap.getHeight());
                bitmap = getResizedBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2);
                chosenImage.setImageBitmap(bitmap);
                finalUri = getImageUri(getApplicationContext(), bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            onBackPressed();
    }

    public boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    STORAGE_PERMISSION_CODE);
            return false;
        }
        return true;
    }

    private void updateMessage(String messageDocumentID, String message) {

        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("edited", true);
        db.collection("iku_earth_messages").document(messageDocumentID)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        messageEntered.setText("");
                        messageEntered.requestFocus();
                        ChatImageActivity.super.onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = getApplicationContext().getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = Math.max(onlyBoundsOptions.outHeight, onlyBoundsOptions.outWidth);

        double ratio = (originalSize > 921600) ? (originalSize / 921600) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
