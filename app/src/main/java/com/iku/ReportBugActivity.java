package com.iku;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iku.databinding.ActivityReportBugBinding;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportBugActivity extends AppCompatActivity {

    private static final String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final ActivityResultLauncher<String[]> requestMultiplePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permsGranted -> {
        if (permsGranted.containsValue(false)) {
            //user denied one or more permissions
            Toast.makeText(this, "PERMISSIONS NOT GRANTED", Toast.LENGTH_SHORT).show();
        } else {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(i, 2);
        }
    });
    ImageView d1, d2, d3;
    EditText messageEntered;
    Button upload;
    String[] myArray = new String[3];
    Uri[] UriArray = new Uri[3];
    List<Uri> myList = new ArrayList<>();
    List<String> finalUrl = new ArrayList<>();
    Uri mainUri;
    private SimpleDateFormat formatter;
    private EditText feedbackText;
    private FirebaseAuth fAuth;
    private FirebaseFirestore firebaseFirestore;
    private String html;
    private String feedbackText_val;
    private String images;
    private String imageSrc;
    private String type;
    private String to;
    private String TAG;
    private String subject;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ProgressDialog mProgress;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView img1, img2, img3, img4;
    private int counter = 0;
    private Date d;
    private ActivityReportBugBinding reportBugBinding;

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize) throws
            FileNotFoundException {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reportBugBinding = ActivityReportBugBinding.inflate(getLayoutInflater());
        setContentView(reportBugBinding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        d = new Date();

        TAG = "ReportBug";
        firebaseFirestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        messageEntered = findViewById(R.id.feedbackText);
        upload = findViewById(R.id.submitButton);
        img1 = findViewById(R.id.firstImage);
        img2 = findViewById(R.id.secondImage);
        img3 = findViewById(R.id.thirdImage);
        img4 = findViewById(R.id.hiddenImageView);
        d1 = findViewById(R.id.delete1);
        d2 = findViewById(R.id.delete2);
        d3 = findViewById(R.id.delete3);

        reportBugBinding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        mStorageRef = FirebaseStorage.getInstance().getReference("bugs");

        img1.setImageResource(R.drawable.addimages);
        img2.setEnabled(false);
        img3.setEnabled(false);
        d1.setVisibility(View.INVISIBLE);
        d2.setVisibility(View.INVISIBLE);
        d3.setVisibility(View.INVISIBLE);

        initProgressDialog();

        final ImageView[] imageBoxes = {img1, img2, img3, img4};
        final ImageView[] deleteButtons = {d1, d2, d3};

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkAllPermissions()) {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(i, 2);
                } else {
                    requestMultiplePermissionLauncher.launch(perms);
                }
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkAllPermissions()) {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(i, 2);
                } else {
                    requestMultiplePermissionLauncher.launch(perms);
                }
            }
        });

        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkAllPermissions()) {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(i, 2);
                } else {
                    requestMultiplePermissionLauncher.launch(perms);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageEntered.getText().toString().isEmpty()) {
                    Toast.makeText(ReportBugActivity.this, "Enter a message!", Toast.LENGTH_SHORT).show();
                } else {
                    mProgress.show();
                    uploadToStorage();
                }
            }
        });


        d1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int k = 0, lastImage = 0;
                myList.remove(k);
                for (int i = k; i < imageBoxes.length - 1; i++) {
                    if (imageBoxes[i + 1].getDrawable() != null) {

                        imageBoxes[i].setImageDrawable(imageBoxes[i + 1].getDrawable());
                        imageBoxes[i].setEnabled(false);
                        deleteButtons[i].setVisibility(View.VISIBLE);
                        deleteButtons[i].setEnabled(true);
                        imageBoxes[i + 1].setImageDrawable(null);
                        if (i <= 1) {
                            deleteButtons[i + 1].setVisibility(View.INVISIBLE);
                            deleteButtons[i + 1].setEnabled(false);
                        }
                        lastImage = i;

                    } else break;

                }
                imageBoxes[lastImage].setEnabled(true);
                if (lastImage <= 2) {
                    deleteButtons[lastImage].setEnabled(false);
                    deleteButtons[lastImage].setVisibility(View.INVISIBLE);
                }

            }
        });
        d2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int k = 1, lastImage = 0;
                myList.remove(k);
                for (int i = k; i < imageBoxes.length - 1; i++) {
                    if (imageBoxes[i + 1].getDrawable() != null) {

                        imageBoxes[i].setImageDrawable(imageBoxes[i + 1].getDrawable());
                        imageBoxes[i].setEnabled(false);
                        deleteButtons[i].setVisibility(View.VISIBLE);
                        deleteButtons[i].setEnabled(true);
                        imageBoxes[i + 1].setImageDrawable(null);
                        if (i <= 1) {
                            deleteButtons[i + 1].setVisibility(View.INVISIBLE);
                            deleteButtons[i + 1].setEnabled(false);
                        }
                        lastImage = i;

                    } else break;

                }
                imageBoxes[lastImage].setEnabled(true);
                if (lastImage <= 2) {
                    deleteButtons[lastImage].setEnabled(false);
                    deleteButtons[lastImage].setVisibility(View.INVISIBLE);
                }

            }
        });
        d3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int k = 2, lastImage = 0;
                myList.remove(k);
                for (int i = k; i < imageBoxes.length - 1; i++) {
                    if (imageBoxes[i + 1].getDrawable() != null) {

                        imageBoxes[i].setImageDrawable(imageBoxes[i + 1].getDrawable());
                        imageBoxes[i].setEnabled(false);
                        deleteButtons[i].setVisibility(View.VISIBLE);
                        deleteButtons[i].setEnabled(true);
                        imageBoxes[i + 1].setImageDrawable(null);
                        if (i <= 1) {
                            deleteButtons[i + 1].setVisibility(View.INVISIBLE);
                            deleteButtons[i + 1].setEnabled(false);
                        }
                        lastImage = i;

                    } else break;

                }
                imageBoxes[lastImage].setEnabled(true);
                if (lastImage <= 2) {
                    deleteButtons[lastImage].setEnabled(false);
                    deleteButtons[lastImage].setVisibility(View.INVISIBLE);
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView[] imageBoxes = {img1, img2, img3, img4};
        ImageView[] deleteButtons = {d1, d2, d3};

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            myList.add(uri);
            imageBoxes[requestCode].setImageDrawable(null);
            imageBoxes[requestCode].setImageURI(uri);
            deleteButtons[requestCode].setEnabled(true);
            imageBoxes[requestCode].setEnabled(false);
            deleteButtons[requestCode].setVisibility(View.VISIBLE);
            if (requestCode <= 2) {
                imageBoxes[requestCode + 1].setImageResource(R.drawable.addimages);
                imageBoxes[requestCode + 1].setEnabled(true);
                if (requestCode <= 1) {
                    deleteButtons[requestCode + 1].setEnabled(false);
                    deleteButtons[requestCode + 1].setVisibility(View.INVISIBLE);
                }
            }


        } else
            onBackPressed();
    }

    private String getFileExtension(Uri uri) {
        if (uri != null) {
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cR.getType(uri));
        } else return null;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "images", null);

        if (path != null) return Uri.parse(path);
        else return null;
    }

    private void uploadToStorage() {
        if (myList.size() == 0 && finalUrl.size() == 0) {
            uploadToDB();
        }
        for (Uri uri : myList) {
            try {
                final Bitmap imageSelected = decodeUri(this, uri, 1080);
                mainUri = getImageUri(ReportBugActivity.this, imageSelected);

                final StorageReference imageRef = mStorageRef.child(user.getUid() + "/" + System.currentTimeMillis() + "." + getFileExtension(mainUri));
                UploadTask uploadTask = imageRef.putFile(uri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> downloadUrl = imageRef.getDownloadUrl();
                        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                counter++;

                                finalUrl.add(uri.toString());

                                if (counter == myList.size()) {
                                    uploadToDB();
                                }
                            }
                        });
                    }


                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadToDB() {

        if (messageEntered.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Err add some details!", Toast.LENGTH_SHORT).show();
            mProgress.dismiss();
        } else {

            Map<String, Object> docData = new HashMap<>();

            formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

            int finalUrl_size = finalUrl.size();
            int num;
            if (finalUrl_size > 0) {
                imageSrc = "";
                for (int i = 0; i < finalUrl_size; i++) {
                    num = i + 1;
                    images += "<tr> <th style=\"text-align:left;\">Image " + num + "</th> <th style=\"text-align:left;\">" + finalUrl.get(i) + "</th> </tr> ";
                    imageSrc += "<h3>Image " + num + "</h3>" + "<img src=\"" + finalUrl.get(i) + "\" style=\"max-height:512px;\"> ";
                }
            }

            feedbackText_val = messageEntered.getText().toString().trim();
            to = "tech@iku.earth";
            subject = "Bug reported by " + user.getDisplayName();
            html = "<style> th { text-align:left; } </style><h3>Bug details</h3>" +
                    "<table> " +
                    "<tr> <th style=\"text-align:left;\">Name:</th> <th style=\"text-align:left;\">" + user.getDisplayName() + "</th> </tr>" +
                    "<tr> <th style=\"text-align:left;\">Version:</th> " + "<th style=\"text-align:left;\">" + BuildConfig.VERSION_CODE + "</th> </tr> " +
                    "<tr> <th style=\"text-align:left;\">Message:</th> <th style=\"text-align:left;\"> <b>" + feedbackText_val + "</b></th> </tr> " +
                    "<tr> <th style=\"text-align:left;\">UID:</th> <th style=\"text-align:left;\">" + user.getUid() + "</th> </tr> " +
                    "<tr> <th style=\"text-align:left;\">Email ID:</th> <th style=\"text-align:left;\">" + user.getEmail() + "</th> </tr> " +
                    "<tr> <th style=\"text-align:left;\">Time:</th> <th style=\"text-align:left;\">" + d.getTime() + "</th> </tr>"
                    + images +
                    " </table>" + imageSrc;
            type = "bug";

            docData.put("to", to);
            Map<String, Object> nestedData = new HashMap<>();
            nestedData.put("html", html);
            nestedData.put("subject", subject);
            docData.put("message", nestedData);
            docData.put("type", "bug");
            docData.put("uid", user.getUid());
            docData.put("timeStamp", new Timestamp(new Date()));

            db.collection("mail").document()
                    .set(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgress.dismiss();
                            messageEntered.setText("");
                            //Log event
                            Bundle bug_bundle = new Bundle();
                            bug_bundle.putString("status", "submitted");
                            bug_bundle.putString("UID", user.getUid());
                            mFirebaseAnalytics.logEvent("bug_report", bug_bundle);

                            Toast.makeText(ReportBugActivity.this, "Ugh.. those pesky bugs!", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgress.dismiss();
                            //Log event
                            Bundle failed_bundle = new Bundle();
                            failed_bundle.putString("status", "failed");
                            failed_bundle.putString("UID", user.getUid());
                            mFirebaseAnalytics.logEvent("bug", failed_bundle);
                            Toast.makeText(ReportBugActivity.this, "err.. can you try that again?", Toast.LENGTH_LONG).show();
                        }
                    });

        }

    }

    private void initProgressDialog() {
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Uploading bug report");
        mProgress.setMessage("We will fix this ASAP!");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }

    /**
     * Compares all permissions in the provided array with the permissions granted to the application.
     *
     * @return true if all listed permissions are held by the application, otherwise false.
     */
    private boolean checkAllPermissions() {
        for (String p : ReportBugActivity.perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}