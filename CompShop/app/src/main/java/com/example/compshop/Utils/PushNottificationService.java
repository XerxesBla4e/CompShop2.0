package com.example.compshop.Utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.compshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class PushNottificationService extends FirebaseMessagingService {
    FirebaseFirestore firestore;

    @SuppressLint("NewApi")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = message.getNotification().getTitle();
        String text = message.getNotification().getBody();
        String CHANNEL_ID = "XERMESSAGE";
        CharSequence name;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Message Notification",
                NotificationManager.IMPORTANCE_HIGH
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        // Context context;
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Bitmap largeicon = BitmapFactory.decodeResource(getResources(), R.drawable.cart);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSound(notificationSoundUri)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        NotificationManagerCompat.from(this).notify(1, notification.build());

        super.onMessageReceived(message);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        // Get the user ID of the currently logged-in user
        String userId = getCurrentUserId();
        firestore = FirebaseFirestore.getInstance();

        if (userId != null) {
            // Create a Map to update the "token" field in the user document
            Map<String, Object> userUpdate = new HashMap<>();
            userUpdate.put("token", token);

            // Update the user document with the new FCM token
            firestore.collection("users")
                    .document(userId)
                    .update(userUpdate)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("TAG", "FCM Token Updated Successfully");
                    })
                    .addOnFailureListener(e -> {
                        // Handle the failure scenario if necessary
                        Log.d("TAG", "FCM Token Update Failed");
                    });
        }
    }

    // Helper method to get the user ID of the currently logged-in user
    private String getCurrentUserId() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return firebaseUser != null ? firebaseUser.getUid() : null;
    }

}
