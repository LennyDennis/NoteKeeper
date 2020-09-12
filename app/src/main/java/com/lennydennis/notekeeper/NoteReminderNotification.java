package com.lennydennis.notekeeper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NoteReminderNotification {

    private static final String NOTIFICATION_TAG = "NoteReminder";

    public static void notify(final Context context,
                              final String noteTitle, final String noteText, int noteId) {

        final Bitmap picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);

        Intent noteActivityIntent = new Intent(context, NoteActivity.class);
        noteActivityIntent.putExtra(NoteActivity.NOTE_ID, noteId);
        PendingIntent noteIntent = PendingIntent.getActivity(context,
                0, noteActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent coursesIntent = PendingIntent.getActivity(context,
                0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent backUpServiceIntent = new Intent(context, NoteBackupService.class);
        backUpServiceIntent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        PendingIntent backupService = PendingIntent.getService(context,0,backUpServiceIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        @SuppressLint("ResourceAsColor") NotificationCompat.Builder notification = new NotificationCompat.Builder(context, App.NOTE_ACTIVITY_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_baseline_assignment_24)
                .setContentTitle("Review Note")
                .setContentText(noteText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(noteText)
                        .setBigContentTitle(noteTitle)
                        .setSummaryText("Review Note"))
                .setColor(R.color.colorAccent)
                .setLargeIcon(picture)
                .setContentIntent(noteIntent)
                .setAutoCancel(true)
                .addAction(0, "View All Notes", coursesIntent)
                .addAction(0,"B ack Up Notes",backupService)
                .setOnlyAlertOnce(true);

        notify(context, notification.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}