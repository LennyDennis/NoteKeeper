package com.jwhh.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.jwhh.notekeeper.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNoteInfo;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mNoteTitle;
    private EditText mNoteText;
    private int mNotePosition;
    private boolean mIsCanceling;
    private String mOriginalNoteCourseID;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinnerCourses = findViewById(R.id.spinner_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> courseInfoArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,courses);
        courseInfoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(courseInfoArrayAdapter);

        readDisplayStateValue();
        saveOriginalStateValue();
        
        mNoteTitle = findViewById(R.id.note_title);
        mNoteText = findViewById(R.id.note_text);

        if(!mIsNewNote) {
            displayNote(mSpinnerCourses, mNoteTitle, mNoteText);
        }
    }

    private void saveOriginalStateValue() {
        if(mIsNewNote)
            return;

        mOriginalNoteCourseID = mNoteInfo.getCourse().getCourseId();
        mOriginalNoteTitle = mNoteInfo.getTitle();
        mOriginalNoteText = mNoteInfo.getText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCanceling){
            if(mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            }else {
                storePreviousNoteValues();
            }
        }else {
            saveNote();
        }
    }

    private void storePreviousNoteValues() {
        CourseInfo course= DataManager.getInstance().getCourse(mOriginalNoteCourseID);
        mNoteInfo.setCourse(course);
        mNoteInfo.setTitle(mOriginalNoteTitle);
        mNoteInfo.setText(mOriginalNoteText);

    }

    private void saveNote() {
        mNoteInfo.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNoteInfo.setText(mNoteTitle.getText().toString());
        mNoteInfo.setText(mNoteText.getText().toString());
    }

    private void displayNote(Spinner spinnerCourses, EditText noteTitle, EditText noteText) {
        List<CourseInfo> courseInfo = DataManager.getInstance().getCourses();
        int courseIndex = courseInfo.indexOf(mNoteInfo.getCourse());
        spinnerCourses.setSelection(courseIndex);
        noteTitle.setText(mNoteInfo.getTitle());
        noteText.setText(mNoteInfo.getText());
    }

    private void readDisplayStateValue() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = position == POSITION_NOT_SET;
        if(mIsNewNote){
            createNewNote();
        }else {
            mNoteInfo = DataManager.getInstance().getNotes().get(position);
        }
    }

    private void createNewNote() {
        DataManager dataManager = DataManager.getInstance();
        mNotePosition = dataManager.createNewNote();
        mNoteInfo = dataManager.getNotes().get(mNotePosition);

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
        }else if(id == R.id.action_cancel){
            mIsCanceling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo courseInfo = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mNoteTitle.getText().toString();
        String text = "Checkout what I have learnt in the pluralsight course. \""+
                courseInfo.getTitle()+"\"\n"+mNoteText.getText();
        Intent newIntent = new Intent(Intent.ACTION_SEND);
        newIntent.setType("message/rfc2822");
        newIntent.putExtra(Intent.EXTRA_SUBJECT,subject);
        newIntent.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(newIntent);
    }
}
