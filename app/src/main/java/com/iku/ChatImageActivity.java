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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iku.app.AppConfig;
import com.iku.databinding.ActivityChatImageBinding;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
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
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private final int PICK_IMAGE = 1;
    private final int STORAGE_PERMISSION_CODE = 10;
    private final String[] appPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private final String TAG = ChatImageActivity.class.getSimpleName();
    byte[] dataSave;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Uri mImageUri;
    private String docId, message, imageUrl;
    private int compressedImageHeight, compressedImageWidth;
    private String originalImageUrl;
    private String originalFileName;
    private ImageButton sendImageChatbtn;

    private ImageView backButton;

    private PhotoView chosenImage;

    private EditText messageEntered;

    private UploadTask uploadTaskOriginal;

    private ActivityChatImageBinding chatImageBinding;

    public static Bitmap decodeUriToBitmap(Context mContext, Uri sendUri) throws IOException {
        Bitmap getBitmap = null;
        InputStream image_stream;
        image_stream = mContext.getContentResolver().openInputStream(sendUri);
        getBitmap = BitmapFactory.decodeStream(image_stream);
        image_stream.close();
        return getBitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatImageBinding = ActivityChatImageBinding.inflate(getLayoutInflater());
        setContentView(chatImageBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mStorageRef = FirebaseStorage.getInstance().getReference(user.getUid());
        db = FirebaseFirestore.getInstance();
        messageEntered = findViewById(R.id.messageTextField);
        backButton = findViewById(R.id.back_button);
        sendImageChatbtn = findViewById(R.id.sendMessageButton);
        chosenImage = findViewById(R.id.chosenImage);

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

        backButton.setOnClickListener(view -> onBackPressed());

        sendImageChatbtn.setOnClickListener(view -> {
            if (!messageEntered.getText().toString().isEmpty()) {
                if (docId.equals("default")) {
                    uploadFile(messageEntered.getText().toString());
                    sendImageChatbtn.setClickable(false);
                } else if (!docId.equals("default")) {
                    updateMessage(docId, messageEntered.getText().toString());
                }
            } else
                Toast.makeText(ChatImageActivity.this, "Caption such empty..much wow!", Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadFile(String message) {
        if (dataSave != null) {

            StorageReference imageRef = mStorageRef.child("IKU-img_" + dateFormatter.format(new Date()) + ".png");
            UploadTask uploadTask = imageRef.putBytes(dataSave);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Task<Uri> downloadUrl = imageRef.getDownloadUrl();
                downloadUrl.addOnSuccessListener(uri -> {
                    Date d = new Date();
                    long timestamp = d.getTime();
                    Map<String, Object> docData = new HashMap<>();
                    docData.put("message", message.trim());
                    docData.put("timestamp", timestamp);
                    docData.put("serverTime", FieldValue.serverTimestamp());
                    docData.put("uid", user.getUid());
                    docData.put("type", "image");
                    docData.put("imageUrl", uri.toString());
                    docData.put("compressedImageWidth", compressedImageWidth);
                    docData.put("compressedImageHeight", compressedImageHeight);
                    docData.put("originalImageUrl", originalImageUrl);
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
                    ArrayList<Object> spamArray = new ArrayList<>();
                    docData.put("spamReportedBy", spamArray);
                    docData.put("spamCount", 0);
                    docData.put("spam", false);
                    docData.put("deleted", false);
                    docData.put("postCommentCount", 0);

                    Map<String, Object> normalMessage = new HashMap<>();
                    normalMessage.put("firstImage", true);

                    db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION)
                            .add(docData)
                            .addOnSuccessListener(documentReference -> {
                                db.collection(AppConfig.USERS_COLLECTION).document(user.getUid()).get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Boolean isFirstImage = (Boolean) document.get("firstImage");
                                                    if (!isFirstImage) {
                                                        Toast.makeText(ChatImageActivity.this, "Aren't you the best", Toast.LENGTH_LONG).show();
                                                        db.collection(AppConfig.USERS_COLLECTION).document(user.getUid())
                                                                .update(normalMessage)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    //Log event
                                                                    Bundle params = new Bundle();
                                                                    params.putString("type", "image");
                                                                    params.putString("uid", user.getUid());
                                                                    mFirebaseAnalytics.logEvent("first_image_message", params);
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                });
                                                    }
                                                }
                                            }
                                        });
                                messageEntered.setText("");
                                messageEntered.requestFocus();
                                ChatImageActivity.super.onBackPressed();

                                //Log event
                                Bundle params = new Bundle();
                                params.putString("type", "image");
                                params.putString("uid", user.getUid());
                                mFirebaseAnalytics.logEvent("messaging", params);
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
    public void onBackPressed() {
        super.onBackPressed();
        if (uploadTaskOriginal != null)
            uploadTaskOriginal.cancel();
        if (originalFileName != null) {
            mStorageRef.child(originalFileName).delete().addOnSuccessListener(aVoid -> {
            }).addOnFailureListener(e -> {
                //Log event
                Bundle params = new Bundle();
                params.putString("type", "image");
                params.putString("message", "image deletion fail because not found.");
                mFirebaseAnalytics.logEvent("image_delete_fail", params);
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            mImageUri = data.getData();

            originalFileName = "IKU-orig_img_" + dateFormatter.format(new Date()) + ".png";
            StorageReference imageOriginalRef = mStorageRef.child(originalFileName);
            uploadTaskOriginal = imageOriginalRef.putFile(mImageUri);
            uploadTaskOriginal.addOnSuccessListener(taskOriginalSnapshot -> {
                Task<Uri> downloadOriginalUrl = imageOriginalRef.getDownloadUrl();
                downloadOriginalUrl.addOnSuccessListener(originalUri -> originalImageUrl = originalUri.toString()).addOnFailureListener(e -> {
                });
            });

            Bitmap bitmap;
            try {
                bitmap = decodeUriToBitmap(getApplicationContext(), mImageUri);
                bitmap = getResizedBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                compressedImageWidth = bitmap.getWidth();
                compressedImageHeight = bitmap.getHeight();
                chosenImage.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                dataSave = baos.toByteArray();
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
        Date d = new Date();
        long timestamp = d.getTime();
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("edited", true);
        map.put("messageUpdateTime", timestamp);
        map.put("readableMessageUpdateTime", FieldValue.serverTimestamp());
        db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
                .update(map)
                .addOnSuccessListener(aVoid -> {
                    messageEntered.setText("");
                    messageEntered.requestFocus();
                    ChatImageActivity.super.onBackPressed();
                })
                .addOnFailureListener(e -> {
                });
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
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
