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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iku.databinding.ActivitySettingsBinding;
import com.iku.models.FeedbackImageModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SettingsActivity extends AppCompatActivity {


    private StorageReference mStorageRef;

    private SimpleDateFormat formatter;

    private CardView goToReportABugActivity;

    ImageView d1, d2, d3;
    EditText messageEntered;
    Button upload;
    int PICK_IMAGE = 1;
    int stars = 0;
    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private FirebaseFirestore db;
    private int STORAGE_PERMISSION_CODE=10;
    private ImageView img1, img2, img3, img4;
    String myArray[] = new String[3];
    Uri UriArray[] = new Uri[3];
    ImageView s[] = new ImageView[5];
    private int counter = 0;
    List<Uri> myList = new ArrayList<>();
    List<String> finalUrl = new ArrayList<>();
    String text;
    private String html;
    private String subject;
    private String images;
    private String imageSrc;
    Uri mainUri;

    private ActivitySettingsBinding settingsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(settingsBinding.getRoot());

        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 0:
                    s[0] = findViewById(R.id.star1);
                case 1:
                    s[1] = findViewById(R.id.star2);
                case 2:
                    s[2] = findViewById(R.id.star3);
                case 3:
                    s[3] = findViewById(R.id.star4);
                case 4:
                    s[4] = findViewById(R.id.star5);
            }
        }

        settingsBinding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        messageEntered = (EditText) findViewById(R.id.feedbackText);
        upload = (Button) findViewById(R.id.submitButton);
        img1 = (ImageView) findViewById(R.id.firstImage);
        img2 = (ImageView) findViewById(R.id.secondImage);
        img3 = (ImageView) findViewById(R.id.thirdImage);
        img4 = (ImageView) findViewById(R.id.hiddenImageView);
        d1 = (ImageView) findViewById(R.id.delete1);
        d2 = (ImageView) findViewById(R.id.delete2);
        d3 = (ImageView) findViewById(R.id.delete3);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        mStorageRef = FirebaseStorage.getInstance().getReference("feedback");

        goToReportABugActivity = findViewById(R.id.report_a_bug_button);

        goToReportABugActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, ReportBugActivity.class);
                startActivity(intent);
            }
        });

        img1.setImageResource(R.drawable.addimages);
        img2.setEnabled(false);
        img3.setEnabled(false);
        d1.setVisibility(View.INVISIBLE);
        d2.setVisibility(View.INVISIBLE);
        d3.setVisibility(View.INVISIBLE);

        final ImageView[] imageBoxes = {img1, img2, img3, img4};
        final ImageView[] deleteButtons = {d1, d2, d3};

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(0);
                } else {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(i, 0);
                }
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(1);
                } else {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(i, 1);
                }
            }
        });

        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(2);
                } else {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(i, 2);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadToStorage();
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

    public void starfn(View view) {
        if (view == s[0]) {
            settingsBinding.feedbackText.setVisibility(View.VISIBLE);
            settingsBinding.imagesLayout.setVisibility(View.VISIBLE);
            settingsBinding.submitButton.setVisibility(View.VISIBLE);
            stars = 1;
        } else if (view == s[1]) {
            settingsBinding.feedbackText.setVisibility(View.VISIBLE);
            settingsBinding.imagesLayout.setVisibility(View.VISIBLE);
            settingsBinding.submitButton.setVisibility(View.VISIBLE);
            stars = 2;
        } else if (view == s[2]) {
            settingsBinding.feedbackText.setVisibility(View.VISIBLE);
            settingsBinding.imagesLayout.setVisibility(View.VISIBLE);
            settingsBinding.submitButton.setVisibility(View.VISIBLE);
            stars = 3;
        } else if (view == s[3]) {
            settingsBinding.feedbackText.setVisibility(View.VISIBLE);
            settingsBinding.imagesLayout.setVisibility(View.VISIBLE);
            settingsBinding.submitButton.setVisibility(View.VISIBLE);
            stars = 4;
        } else if (view == s[4]) {
            stars = 5;
            settingsBinding.feedbackText.setVisibility(View.VISIBLE);
            settingsBinding.imagesLayout.setVisibility(View.VISIBLE);
            settingsBinding.submitButton.setVisibility(View.VISIBLE);
        }
        starfnutil(stars - 1);
    }

    public void starfnutil(int stars) {
        for (int i = 0; i <= stars; i++) {
            s[i].setImageResource(R.drawable.ic_filled_star);
            Log.i("zero", Integer.toString(i) + "clicked");
        }
        for (int i = stars + 1; i < 5; i++) {
            s[i].setImageResource(R.drawable.ic_unfilled_star);
        }
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


    private void uploadToStorage() {
        if (myList.size()==0 && finalUrl.size()==0){
            uploadToDB();
        }
        for (Uri uri : myList) {
            try {
                final Bitmap imageSelected = decodeUri(this, uri, 1080);
                mainUri = getImageUri(SettingsActivity.this, imageSelected);

                final StorageReference imageRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mainUri));
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

                                //Toast.makeText(SettingsActivity.this, "uri added", Toast.LENGTH_LONG).show();
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

        Date d = new Date();
        long timestamp = d.getTime();
        formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

        int finalUrl_size= finalUrl.size();
        int num;
        if(finalUrl_size > 0) {
            imageSrc = "";
            for (int i = 0; i < finalUrl_size; i++) {
                num = i+1;
                images +=  "<tr> <th style=\"text-align:left;\" >Image " + num +"</th> <th style=\"text-align:left;\">"+  finalUrl.get(i) + "</th> </tr> ";
                imageSrc += "<h3>Image "+ num +"</h3>" + "<img src=\"" + finalUrl.get(i) + "\" style=\"max-height:512px;\"> ";
            }
        }

        Map<String, Object> docData = new HashMap<>();
        subject = "Feedback by " + user.getDisplayName();
        html = "<h3>Feedback details:</h3>" +
                "<table> " +
                "<tr> <th style=\"text-align:left;\">Name: </th> <th style=\"text-align:left;\">" + user.getDisplayName() + "</th> </tr>" +
                "<tr> <th style=\"text-align:left;\">Version: </th> " + "<th style=\"text-align:left;\" >" + BuildConfig.VERSION_CODE + "</th> </tr> " +
                "<tr> <th style=\"text-align:left;\">Rating: </th> " + "<th style=\"text-align:left;\" >" + stars + "</th> </tr> " +
                "<tr> <th style=\"text-align:left;\">Message: </th> <th style=\"text-align:left;\"> <b>" + messageEntered.getText().toString().trim() + "</b></th> </tr> " +
                "<tr> <th style=\"text-align:left;\">UID: </th> <th style=\"text-align:left;\">" + user.getUid() + "</th> </tr> " +
                "<tr> <th style=\"text-align:left;\">Email ID: </th> <th style=\"text-align:left;\">" + user.getEmail() + "</th> </tr> " +
                "<tr> <th style=\"text-align:left;\">Time: </th> <th style=\"text-align:left;\">" + formatter.format(timestamp) + "</th> </tr>" + images +
                " </table>"+ imageSrc;
        FeedbackImageModel feedbackImageModel = new FeedbackImageModel(subject, html);
        docData.put("message", feedbackImageModel);
        docData.put("rating", stars);
        docData.put("timestamp", new Timestamp(new Date()));
        docData.put("to", "tech@iku.earth");
        docData.put("type", "feedback");
        docData.put("uid", user.getUid());

        db.collection("mail")
                .add(docData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        messageEntered.setText("");
                        Toast.makeText(SettingsActivity.this, "Thank you for that!", Toast.LENGTH_LONG).show();
                        //SaveImage(imageSelected);
                        stars = 0;
                        for (int i = 0; i < 5; i++) {
                            s[i].setImageResource(R.drawable.ic_unfilled_star);
                        }
                        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "err.. can you try that again?", Toast.LENGTH_LONG).show();
                        //Toast.makeText(SettingsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void requestStoragePermission(int code) {

        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, code);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, 0);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == 1)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, 1);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == 2)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, 2);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}