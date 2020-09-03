package com.lennydennis.notekeeper;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String NOTE_ACTIVITY_NOTIFICATION = "Note";

    @Override
    public void onCreate() {
        super.onCreate();
        
        createNotificationChannels();
        
    }

    private void createNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTE_ACTIVITY_NOTIFICATION,
                    "NoteKeeper",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setDescription("This is a dummy text");

            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(notificationChannel);

        }
    }
}
