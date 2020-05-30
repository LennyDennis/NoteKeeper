package com.jwhh.notekeeper;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static org.hamcrest.Matchers.*;
import static androidx.test.espresso.Espresso.pressBack;

@RunWith(AndroidJUnit4.class)

public class NoteCreationTest {
    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp(){
        sDataManager = DataManager.getInstance();
    }

    @Rule
    public ActivityTestRule<NoteListActivity> mNoteListActivityActivityTestRule = new ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void createNewNote(){
        final CourseInfo courseInfo = sDataManager.getCourse("java_lang");
        final String noteTitle = "Title";
        final String noteText = "This is the text";
//        ViewInteraction fabNewNote = onView(withId(R.id.fab));
//        fabNewNote.perform(click());
        onView(withId(R.id.fab)).perform(click());

        onView(withId(R.id.spinner_courses)).perform(click());

        onData(allOf(instanceOf(CourseInfo.class), equalTo(courseInfo))).perform(click());
        onView(withId(R.id.note_title)).perform(typeText(noteTitle));
        onView(withId(R.id.note_text)).perform(typeText(noteText),
                closeSoftKeyboard());

        pressBack();
    }

}