package com.iku.service;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iku.HomeActivity;
import com.iku.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IkuFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = IkuFirebaseMessagingService.class.getSimpleName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private long timeStamp;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String notificationBody = remoteMessage.getNotification().getBody();
            String notificationTitle = remoteMessage.getNotification().getTitle();
            if (isForeground(getApplicationContext())) {
            } else {
                //if in background then perform notification operation
                sendUpvotesNotification(notificationTitle, notificationBody);
            }
        }
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ArrayList<String> titleList = new ArrayList<>();
        ArrayList<String> messageList = new ArrayList<>();

        db.collection("users").document(mAuth.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        timeStamp = (long) document.get("lastSeen");

                        db.collection("iku_earth_messages").whereGreaterThan("timestamp", timeStamp).orderBy("timestamp", Query.Direction.DESCENDING).limit(4)
                                .get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                    titleList.add((String) document1.get("userName"));
                                    messageList.add((String) document1.get("message"));
                                }
                                if (isForeground(getApplicationContext())) {
                                } else {
                                    //if in background then perform notification operation
                                    sendMessageNotification(title, message, titleList, messageList);
                                }
                            }
                        });
                    }
                });
    }

    private void sendUpvotesNotification(String title, String message) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "iku_hearts")
                .setSmallIcon(R.drawable.ic_iku)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(title))
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(999, builder.build());

    }

    private void sendMessageNotification(String title, String message, ArrayList titles, ArrayList messages) {
        Intent resultIntent = new Intent(this, HomeActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent piResult = PendingIntent.getActivity(this, 0, resultIntent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_iku)
                .setContentTitle("#IkuExperiment")
                .setContentIntent(piResult);

        Notification.InboxStyle notification = new Notification.InboxStyle();
        notification.setBigContentTitle("#IkuExperiment");

        if (titles.size() > 0) {
            for (int i = titles.size() - 1; i >= 0; i--) {
                notification.addLine(titles.get(i) + ": " + messages.get(i));
            }
        }

        builder.setStyle(notification);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(121, builder.build());
    }

    @Override
    public void onNewToken(@NotNull String token) {

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    public void sendRegistrationToServer(String token) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (user != null) {
            user = mAuth.getCurrentUser();
            Map<String, Object> userRegistrationTokenInfo = new HashMap<>();
            userRegistrationTokenInfo.put("registrationToken", token);
            userRegistrationTokenInfo.put("uid", user.getUid());
            db.collection("registrationTokens").document(user.getUid())
                    .set(userRegistrationTokenInfo)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    private static boolean isForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : tasks) {
            if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND == appProcess.importance && packageName.equals(appProcess.processName)) {
                return true;
            }
        }
        return false;
    }
}