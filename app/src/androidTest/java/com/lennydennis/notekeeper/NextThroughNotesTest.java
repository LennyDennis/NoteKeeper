package com.lennydennis.notekeeper;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.*;


public class NextThroughNotesTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void NextThroughNotes(){
      onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
      onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));

      onView(withId(R.id.list_items)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));

        List<NoteInfo> noteInfos = DataManager.getInstance().getNotes();
        for(int index = 0;index<noteInfos.size();index++) {
            NoteInfo noteInfo = noteInfos.get(index);

            onView(withId(R.id.spinner_courses)).check(
                    matches(withSpinnerText(noteInfo.getCourse().getTitle())));

            onView(withId(R.id.note_title)).check(matches(withText(noteInfo.getTitle())));
            onView(withId(R.id.note_text)).check(matches(withText(noteInfo.getText())));

            if(index<noteInfos.size()-1) {
                onView(allOf(withId(R.id.action_next), isEnabled())).perform(click());
            }
        }
        onView(withId(R.id.action_next)).check(matches(not(isEnabled())));
        pressBack();
    }
}