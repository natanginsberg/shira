package com.function.karaoke.hardware;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.core.model.Song;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.SongDisplay;
import com.function.karaoke.hardware.fragments.SongsListFragment.OnListFragmentInteractionListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {

    // there can be other sort strategies and filters
//    private static final Comparator<Song> mComparator = new Comparator<Song>() {
//        @Override
//        public int compare(Song a, Song b) {
//            if (!a.artist.equalsIgnoreCase(b.artist))
//                return a.artist.compareToIgnoreCase(b.artist);
//            return a.title.compareToIgnoreCase(b.title);
//        }
//    };

    private static final Comparator<SongDisplay> mComparator = new Comparator<SongDisplay>() {
        @Override
        public int compare(SongDisplay a, SongDisplay b) {
            if (!a.getArtist().equalsIgnoreCase(b.getArtist()))
                return a.getArtist().compareToIgnoreCase(b.getArtist());
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        }
    };

    private List<SongDisplay> mValues;
    private List<Recording> mRecordings;
    private final OnListFragmentInteractionListener mListener;
    private String language;
    private String text;

    //    public SongRecyclerViewAdapter(List<Song> items, OnListFragmentInteractionListener listener, String language) {
//        setData(items);
//        mListener = listener;
//        this.language = language;
//    }
    public SongRecyclerViewAdapter(List<? extends SongDisplay> items, OnListFragmentInteractionListener listener, String language, String textToDisplay) {
        setData(items, textToDisplay);
        mListener = listener;
        this.language = language;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (language.equals("English")) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_song_list_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.hebrew_resource_file, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setItem(mValues.get(position));

        // making the click only on the button and not on the whole icon

        holder.itemView.findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(List<? extends SongDisplay> songs, String textToDisplay) {
        mValues = new ArrayList<>(songs); // make a copy
        Collections.sort(mValues, mComparator);
        text = textToDisplay;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mLblTitle;
        private final TextView mLblArtist;
        private final ImageView mCover;

        //        public Song mItem;
        public SongDisplay mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLblTitle = view.findViewById(R.id.lbl_title);
            mLblArtist = view.findViewById(R.id.lbl_artist);
            mCover = view.findViewById(R.id.img_cover);
            ((Button)view.findViewById(R.id.play_button)).setText(text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLblArtist.getText() + "'";
        }

        public void setItem(SongDisplay song) {
//            Typeface tf = Typeface.createFromAsset(mView.getContext().getAssets(),"fonts/ArialUnicodeMS.ttf");
            mItem = song;
            mLblTitle.setText(song.getTitle());
//            mLblTitle.setTypeface(tf);
//            tf = Typeface.createFromAsset(mView.getContext().getAssets(),"fonts/ArialUnicodeMS.ttf");
            mLblArtist.setText(song.getArtist());
//            mLblArtist.setTypeface(tf);
            Picasso.get()
                    .load(song.getImageResourceFile())
                    .placeholder(R.drawable.ic_cover_empty)
                    .fit()
                    .into(mCover);
        }
    }

}
