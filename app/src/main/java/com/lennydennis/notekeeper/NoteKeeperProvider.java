package com.lennydennis.notekeeper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.lennydennis.notekeeper.Database.NoteKeeperDatabaseContract;
import com.lennydennis.notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.lennydennis.notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.lennydennis.notekeeper.Database.NoteKeeperOpenHelper;
import com.lennydennis.notekeeper.NoteKeeperProviderContract.CourseIdColumns;
import com.lennydennis.notekeeper.NoteKeeperProviderContract.Courses;
import com.lennydennis.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteKeeperProvider extends ContentProvider {

    private static final String MIME_VENDOR_TYPE = "vnd." + NoteKeeperProviderContract.AUTHORITY + ".";
    private NoteKeeperOpenHelper mNoteKeeperOpenHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int COURSES = 0;

    private static final int NOTES = 1 ;

    private static final int EXPANDED = 2;

    private static final int NOTES_ROW = 3;

    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, EXPANDED);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
       String mimeType = null;
       int uriMatch = sUriMatcher.match(uri);
       switch (uriMatch) {
           case COURSES:
               mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                       MIME_VENDOR_TYPE + Courses.PATH;
               break;
           case NOTES:
               mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                       MIME_VENDOR_TYPE + Notes.PATH;
               break;
           case EXPANDED:
               mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                       MIME_VENDOR_TYPE + Notes.PATH_EXPANDED;
               break;
           case NOTES_ROW:
               mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/"+
                       MIME_VENDOR_TYPE+Notes.PATH;
               break;

       }
       return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = mNoteKeeperOpenHelper.getWritableDatabase();
        long rowId = -1;
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case NOTES:
                rowId = database.insert(NoteInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI,rowId);
                 break;
            case COURSES:
                rowId = database.insert(CourseInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI,rowId);
                break;
            case EXPANDED:
                break;

        }

        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mNoteKeeperOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase database = mNoteKeeperOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case COURSES:
                cursor = database.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTES:
                cursor = database.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case EXPANDED:
                cursor = notesExpandedQuery(database, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + "= ?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = database.query(NoteInfoEntry.TABLE_NAME,projection,rowSelection,rowSelectionArgs,null,null,null);
                break;
        }
        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase database, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        String[] columns = new String[projection.length];
        for(int i = 0; i < projection.length ; i++){
            columns[i] = projection[i].equals(BaseColumns._ID) ||
                    projection[i].equals(CourseIdColumns.COLUMN_COURSE_ID)?
                    NoteInfoEntry.getQName(projection[i]) : projection[i];
        }
        String tablesWIthJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
                CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID)+ " = " +
                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

        return database.query(tablesWIthJoin, columns, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
