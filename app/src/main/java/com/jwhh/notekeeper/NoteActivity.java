package com.jwhh.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_INFO = "com.jwhh.notekeeper.NOTE_INFO";
    private NoteInfo mNoteInfo;
    private boolean mIsNewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Spinner spinnerCourses = findViewById(R.id.spinner_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> courseInfoArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,courses);
        courseInfoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(courseInfoArrayAdapter);

        readDisplayStateValue();

        EditText noteTitle = findViewById(R.id.note_title);
        EditText noteText = findViewById(R.id.note_text);

        if(!mIsNewNote) {
            displayNote(spinnerCourses, noteTitle, noteText);
        }
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
        mNoteInfo = intent.getParcelableExtra(NOTE_INFO);
        mIsNewNote = mNoteInfo == null;
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
