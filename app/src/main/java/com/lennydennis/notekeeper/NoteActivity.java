package com.lennydennis.notekeeper;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.lennydennis.notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.lennydennis.notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.lennydennis.notekeeper.Database.NoteKeeperOpenHelper;
import com.lennydennis.notekeeper.NoteKeeperProviderContract.Courses;
import com.lennydennis.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_NOTES = 0;
    private static final int LOADER_COURSES = 1;
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "com.lennydennis.notekeeper.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    private NoteInfo mNoteInfo = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mNoteTitle;
    private EditText mNoteText;
    private int mNoteId;
    private boolean mIsCanceling;
    private NoteActivityViewModel mViewModel;
    private NoteKeeperOpenHelper mNoteKeeperOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mSimpleCursorAdapter;
    private boolean mCourseQueryFinished;
    private boolean mNoteQueryFinished;
    private Uri mNoteUri;
    private NotificationManagerCompat mNotificationManagerCompat;

    @Override
    protected void onDestroy() {
        mNoteKeeperOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNotificationManagerCompat = NotificationManagerCompat.from(this);

        mNoteKeeperOpenHelper = new NoteKeeperOpenHelper(this);

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(), ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if (mViewModel.mIsNewlyCreated && savedInstanceState != null) {
            mViewModel.restoreState(savedInstanceState);
        }
        mViewModel.mIsNewlyCreated = false;

        mSpinnerCourses = findViewById(R.id.spinner_courses);

        mSimpleCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE}, new int[]{android.R.id.text1}, 0);
        mSimpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mSimpleCursorAdapter);

        LoaderManager.getInstance(this).initLoader(LOADER_COURSES, null, this);

        readDisplayStateValue();
        saveOriginalStateValue();

        mNoteTitle = findViewById(R.id.note_title);
        mNoteText = findViewById(R.id.note_text);

        if (!mIsNewNote)
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this);

        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null) {
            mViewModel.saveState(outState);
        }
    }

    private void saveOriginalStateValue() {
        if (mIsNewNote)
            return;

        mViewModel.mOriginalNoteCourseID = mNoteInfo.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNoteInfo.getTitle();
        mViewModel.mOriginalNoteText = mNoteInfo.getText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCanceling) {
            Log.i(TAG, "onPause: Cancelling note at position" + mNoteId);
            if (mIsNewNote) {
                deleteNoteFromDatabase();
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
        Log.d(TAG, "onPause");
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        @SuppressLint("StaticFieldLeak") AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getWritableDatabase();
                sqLiteDatabase.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                return null;
            }
        };

        task.execute();

    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseID);
        mNoteInfo.setCourse(course);
        mNoteInfo.setTitle(mViewModel.mOriginalNoteTitle);
        mNoteInfo.setText(mViewModel.mOriginalNoteText);

    }

    private void saveNote() {
        String courseId = selectedCourseID();
        String noteTitle = mNoteTitle.getText().toString();
        String noteText = mNoteText.getText().toString();
        saveNoteToDatabase(courseId, noteTitle, noteText);

    }

    private String selectedCourseID() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mSimpleCursorAdapter.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    public void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        contentValues.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        contentValues.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getWritableDatabase();
        sqLiteDatabase.update(NoteInfoEntry.TABLE_NAME, contentValues, selection, selectionArgs);
    }

    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        int courseIndex = getIndexOfCourseId(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mNoteTitle.setText(noteTitle);
        mNoteText.setText(noteText);

        CourseEventBroadcastHelper.sendEventBroadcast(this,courseId,"Editing note");
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mSimpleCursorAdapter.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more) {
            String cursorCourseId = cursor.getString(courseIdPos);
            if (cursorCourseId.equals(courseId))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValue() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }

        Log.i(TAG, "readDisplayStateValue: " + mNoteId);
    }

    private void createNewNote() {
        @SuppressLint("StaticFieldLeak") AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {
            private ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                mProgressBar = findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                ContentValues insertValues = contentValues[0];
                Uri rowUri = getContentResolver().insert(Notes.CONTENT_URI, insertValues);

                try {
                    simulate5Seconds();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(2);

                try {
                     simulate5Seconds();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(3);

                return rowUri;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int value = values[0];
                mProgressBar.setProgress(value);
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                mNoteUri = uri;
                mProgressBar.setVisibility(View.GONE);
            }
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(Notes.COLUMN_COURSE_ID, "");
        contentValues.put(Notes.COLUMN_NOTE_TITLE, "");
        contentValues.put(Notes.COLUMN_NOTE_TEXT, "");

        task.execute(contentValues);

    }

    private void simulate5Seconds() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCanceling = true;
            finish();
        } else if (id == R.id.action_next) {
            moveNext();
        } else if (id == R.id.action_set_reminder) {
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
        final Bitmap picture = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_info_24);
        String noteText = mNoteText.getText().toString();
        String noteTitle = mNoteTitle.getText().toString();
        int noteId = (int) ContentUris.parseId(mNoteUri);
        Intent noteActivityIntent = new Intent(this, NoteActivity.class);
        noteActivityIntent.putExtra(NoteActivity.NOTE_ID, noteId);
        PendingIntent noteIntent = PendingIntent.getActivity(this,
                0, noteActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent coursesIntent = PendingIntent.getActivity(this,
                0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent backUpServiceIntent = new Intent(this, NoteBackupService.class);
        backUpServiceIntent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        PendingIntent backupService = PendingIntent.getService(this,0,backUpServiceIntent,PendingIntent.FLAG_UPDATE_CURRENT);



        @SuppressLint("ResourceAsColor") Notification notification = new NotificationCompat.Builder(this, App.NOTE_ACTIVITY_NOTIFICATION)
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
                .setOnlyAlertOnce(true)
                .build();

        mNotificationManagerCompat.notify(1, notification);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        menuItem.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }


    private void moveNext() {
        saveNote();

        ++mNoteId;
        mNoteInfo = DataManager.getInstance().getNotes().get(mNoteId);
        saveOriginalStateValue();
        displayNote();
        invalidateOptionsMenu();
    }

    private void sendEmail() {
        CourseInfo courseInfo = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mNoteTitle.getText().toString();
        String text = "Checkout what I have learnt in the pluralsight course. \"" +
                courseInfo.getTitle() + "\"\n" + mNoteText.getText();
        Intent newIntent = new Intent(Intent.ACTION_SEND);
        newIntent.setType("message/rfc2822");
        newIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        newIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(newIntent);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader cursorLoader = null;
        if (id == LOADER_NOTES)
            cursorLoader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            cursorLoader = createLoaderCourses();
        return cursorLoader;
    }

    private CursorLoader createLoaderCourses() {
        mCourseQueryFinished = false;
        Uri uri = Courses.CONTENT_URI;

        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };

        return new CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);

    }

    private CursorLoader createLoaderNotes() {
        mNoteQueryFinished = false;
        String[] noteColumns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, mNoteUri, noteColumns, null, null, null);

    }

    ;

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES) {
            loadFinishedNotes(data);
        } else if (loader.getId() == LOADER_COURSES) {
            mSimpleCursorAdapter.changeCursor(data);
            mCourseQueryFinished = true;
            displayNoteWhenQueriesFinish();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToFirst();
        mNoteQueryFinished = true;
        displayNoteWhenQueriesFinish();

    }

    private void displayNoteWhenQueriesFinish() {
        if (mNoteQueryFinished && mCourseQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES) {
            if (mNoteCursor != null) {
                mNoteCursor.close();
            }
        } else if (loader.getId() == LOADER_COURSES) {
            mSimpleCursorAdapter.changeCursor(null);
        }
    }
}
