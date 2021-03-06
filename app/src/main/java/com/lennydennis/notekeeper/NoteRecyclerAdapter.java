package com.lennydennis.notekeeper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lennydennis.notekeeper.Database.NoteKeeperDatabaseContract;
import com.lennydennis.notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.lennydennis.notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int mCoursePos;
    private int mNoteTilePos;
    private int mIdPos;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);

        populateColumnPosition();
    }

    private void populateColumnPosition() {
        if (mCursor == null)
            return;
        mCoursePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mNoteTilePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);
    }

    public void changeCursor(Cursor cursor) {
        mCursor = cursor;
        populateColumnPosition();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextCourse = itemView.findViewById(R.id.text_course);
            mTextTitle = itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, mId);
                    mContext.startActivity(intent);

                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemVIew = mLayoutInflater.inflate(R.layout.item_note_list, parent, false);
        return new ViewHolder(itemVIew);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String courseId = mCursor.getString(mCoursePos);
        String noteTitle = mCursor.getString(mNoteTilePos);
        int id = mCursor.getInt(mIdPos);

        holder.mTextCourse.setText(courseId);
        holder.mTextTitle.setText(noteTitle);
        holder.mId = id;
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

}
