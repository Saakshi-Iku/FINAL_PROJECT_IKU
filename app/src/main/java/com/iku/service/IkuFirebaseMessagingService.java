package com.iku.service;


import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iku.HomeActivity;
import com.iku.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IkuFirebaseMessagingService extends FirebaseMessagingService {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String notificationTitle = remoteMessage.getNotification().getTitle();
            String notificationBody = remoteMessage.getNotification().getBody();
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
            if (!isForeground(getApplicationContext())) {
                Intent intent = new Intent(this, HomeActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "iku_hearts")
                        .setSmallIcon(R.drawable.ic_iku)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationBody)
                        .setContentIntent(pendingIntent);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(123, builder.build());
            }
        }
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