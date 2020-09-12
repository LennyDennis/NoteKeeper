package com.lennydennis.notekeeper;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

import java.net.URI;

public class NoteActivityViewModel extends ViewModel {
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.lennydennis.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.lennydennis.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.lennydennis.notekeeper.ORIGINAL_NOTE_TEXT";
    private static final String NOTE_URI = "NOTE URI";

    public String mOriginalNoteCourseID;
    public String mOriginalNoteTitle;
    public String mOriginalNoteText;
    public Boolean mIsNewlyCreated = true;
    public URI mURI;


    public void saveState(Bundle outState) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID,mOriginalNoteCourseID);
        outState.putString(ORIGINAL_NOTE_TITLE,mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT,mOriginalNoteText);
//        outState.putString(NOTE_URI,mURI.toString());
    }
    public void restoreState(Bundle inState){
        mOriginalNoteCourseID = inState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = inState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = inState.getString(ORIGINAL_NOTE_TEXT);
    }
}
