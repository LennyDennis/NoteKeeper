package com.jwhh.notekeeper;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>{

    private final Context mContext;
    private final List<NoteInfo> mNoteInfo;
    private final LayoutInflater mLayoutInflater;

    public NoteRecyclerAdapter(Context context, List<NoteInfo> noteInfo) {
        mContext = context;
        mNoteInfo = noteInfo;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int currentPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextCourse = itemView.findViewById(R.id.text_course);
            mTextTitle = itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_POSITION, currentPosition);
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
        NoteInfo noteInfo = mNoteInfo.get(position);
        holder.mTextCourse.setText(noteInfo.getCourse().getTitle());
        holder.mTextTitle.setText(noteInfo.getTitle());
        holder.currentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mNoteInfo.size();
    }



}
