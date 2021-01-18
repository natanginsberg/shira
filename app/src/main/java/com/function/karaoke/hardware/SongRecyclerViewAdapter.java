package com.function.karaoke.hardware;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.core.model.Song;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.Reocording;
import com.function.karaoke.hardware.fragments.SongsListFragment.OnListFragmentInteractionListener;
import com.function.karaoke.hardware.utils.static_classes.OnSwipeTouchListener;
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

    private static final Comparator<Reocording> mComparator = new Comparator<Reocording>() {
        @Override
        public int compare(Reocording a, Reocording b) {
            if (!a.getTitle().equalsIgnoreCase(b.getTitle()))
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            return a.getArtist().compareToIgnoreCase(b.getArtist());
        }
    };

    private List<Reocording> mValues;
    private List<Recording> mRecordings;
    private final OnListFragmentInteractionListener mListener;
    private String language;
    private String text;

    //    public SongRecyclerViewAdapter(List<Song> items, OnListFragmentInteractionListener listener, String language) {
//        setData(items);
//        mListener = listener;
//        this.language = language;
//    }
    public SongRecyclerViewAdapter(List<? extends Reocording> items, OnListFragmentInteractionListener listener, String language, String textToDisplay) {
        setData(items, textToDisplay);
        mListener = listener;
        this.language = language;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_song_list_item, parent, false);
        if (language.equals("iw"))
            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        else
            view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (parent.getHeight() / 8);
        view.setLayoutParams(layoutParams);

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
                    mListener.onListFragmentInteractionPlay(holder.mItem);
                }
            }
        });
//        holder.itemView.setOnTouchListener(new OnSwipeTouchListener(holder.itemView.getContext()) {
//            @Override
//            public void onSwipeRight() {
//                super.onSwipeRight();
//                mListener.colorNextGenre();
//            }
//
//            @Override
//            public void onSwipeLeft() {
//                super.onSwipeLeft();
//                mListener.colorPreviousGenre();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(List<? extends Reocording> songs, String textToDisplay) {
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
        public Reocording mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLblTitle = view.findViewById(R.id.lbl_title);
            mLblArtist = view.findViewById(R.id.lbl_artist);
            mCover = view.findViewById(R.id.img_cover);
            ((TextView) view.findViewById(R.id.play_button)).setText(text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLblArtist.getText() + "'";
        }

        public void setItem(Reocording song) {
            mItem = song;
            mLblTitle.setText(song.getTitle());
            mLblArtist.setText(song.getArtist());
//            if (!language.equals("English")) {
            Typeface tf = Typeface.createFromAsset(mView.getContext().getAssets(), "fonts/SecularOne_Regular.ttf");
            mLblTitle.setTypeface(tf);
            mLblArtist.setTypeface(tf);
            ((TextView) mView.findViewById(R.id.play_button)).setTypeface(tf);
//            }
//            mLblArtist.setTypeface(tf);
            if (!song.getImageResourceFile().equals(""))
                Picasso.get()
                        .load(song.getImageResourceFile())
                        .placeholder(R.drawable.plain_rec)
                        .fit()
                        .into(mCover);
        }
    }

}
