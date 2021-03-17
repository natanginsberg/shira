package com.function.karaoke.interaction.adapters;

import android.annotation.SuppressLint;
import android.graphics.BlendMode;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.core.model.Song;
import com.function.karaoke.interaction.R;
import com.function.karaoke.interaction.activities.Model.DatabaseSong;
import com.function.karaoke.interaction.activities.Model.Recording;
import com.function.karaoke.interaction.fragments.SongsListFragment.OnListFragmentInteractionListener;
import com.function.karaoke.interaction.utils.static_classes.Converter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {

    private static final Comparator<DatabaseSong> mComparator = (a, b) -> {
        if (!a.getTitle().equalsIgnoreCase(b.getTitle()))
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        return a.getArtist().compareToIgnoreCase(b.getArtist());
    };
    private static final int[] rectangles = new int[]{R.drawable.custom_song_rec_1,
            R.drawable.custom_song_rec_2, R.drawable.custom_song_rec_3, R.drawable.custom_song_rec_4};
    private static final int[] transparentRectangles = new int[]{R.drawable.custom_song_rec_1_t,
            R.drawable.custom_song_rec_2_t, R.drawable.custom_song_rec_3_t, R.drawable.custom_song_rec_4_t};
    private static final int[] layouts = new int[]{R.layout.song_display_big, R.layout.song_display_small};
    private static final double[] heightFactors = new double[]{2.5, 3.5};
    private final OnListFragmentInteractionListener mListener;
    private List<DatabaseSong> mValues;
    private List<Recording> mRecordings;
    private double averageSongsPlayed;

    public SongRecyclerViewAdapter(List<? extends DatabaseSong> items, OnListFragmentInteractionListener listener) {
        setData(items);
        mListener = listener;
    }

    public void setAverage(double averageSongsPlayed) {
        this.averageSongsPlayed = averageSongsPlayed;
    }


    @Override
    public int getItemViewType(final int position) {
        return Math.random() < 0.5 ? 0 : 1;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int randomColor = Math.random() < 0.5 ? 0 : 1;
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_display_big, parent, false);
//        if (language.equals("iw"))
//            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
//        else
//            view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int width = (int) (parent.getWidth() / 2.04);
        layoutParams.width = (width);
        layoutParams.height = (int) (1.3 * width);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.findViewById(R.id.song_placeholder).setBackground(parent.getContext().getResources().getDrawable(rectangles[viewType * 2 + randomColor], null));
            view.findViewById(R.id.song_placeholder).setBackgroundTintBlendMode(BlendMode.COLOR_DODGE);
        } else {
            view.findViewById(R.id.song_placeholder).setBackground(parent.getContext().getResources().getDrawable(transparentRectangles[viewType * 2 + randomColor]));
        }
        view.setLayoutParams(layoutParams);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
//        DatabaseSong song = mValues.get(position);
//        ShapeableImageView mCover = (ShapeableImageView) holder.mCover;
        holder.setItem(holder.mView, mValues.get(position));

        holder.itemView.setOnClickListener(view -> {
            if (null != mListener) {
                mListener.onListFragmentInteractionPlay(holder.mItem);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(List<? extends DatabaseSong> songs) {
        mValues = new ArrayList<>(songs); // make a copy
        Collections.sort(mValues, mComparator);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //        public Song mItem;
        public DatabaseSong mItem;
        private final View mView;
        private TextView mLblTitle;
        private TextView mLblArtist;
        private String url;

        public ViewHolder(View view) {
            super(view);
            this.mView = view;

//            ((TextView) view.findViewById(R.id.play_button)).setText(text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLblArtist.getText() + "'" + mLblTitle.getText() + "'" + url;
        }

        public void setItem(View mView, DatabaseSong song) {
            if (mView == this.mView) {
                url = song.getImageResourceFile();
                mLblTitle = mView.findViewById(R.id.lbl_title);
                mLblArtist = mView.findViewById(R.id.lbl_artist);
                ShapeableImageView mCover = mView.findViewById(R.id.img_cover);
                setPopularTag(song);
                setNewTag(song);
                mItem = song;
                mLblTitle.setText(song.getTitle());
                mLblArtist.setText(song.getArtist());
//            if (!language.equals("English")) {
                if (!song.getImageResourceFile().equals("") && !song.getImageResourceFile().equals("no image resource")) {
                    Picasso.get()
                            .load(song.getImageResourceFile())
//                            .load("https://firebasestorage.googleapis.com/v0/b/shira-8ed6f.appspot.com/o/images%2FIMG_4196.JPG?alt=media&token=95a20ae9-19f3-45ad-b2b0-a183a41f969f")
                            .placeholder(R.drawable.ashira)
                            .fit()
                            .into(mCover);
                }
                mCover.setShapeAppearanceModel(mCover.getShapeAppearanceModel()
                        .toBuilder()
                        .setTopRightCorner(CornerFamily.ROUNDED, Converter.convertDpToPx(17))
                        .setTopLeftCorner(CornerFamily.ROUNDED, Converter.convertDpToPx(17))
                        .build());
            }
        }

        private void setNewTag(DatabaseSong song) {
            if (song.getDate().equals("")) {
                mView.findViewById(R.id.new_song_tag).setVisibility(View.INVISIBLE);
                return;
            }

            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            String lastNewDate = String.valueOf(new SimpleDateFormat("yyyy-MM-dd",
                    Locale.getDefault()).format(calendar.getTime().getTime()));
            if (song.getDate().compareTo(lastNewDate) < 0) {
                mView.findViewById(R.id.new_song_tag).setVisibility(View.VISIBLE);
            } else
                mView.findViewById(R.id.new_song_tag).setVisibility(View.INVISIBLE);
        }

        private void setPopularTag(DatabaseSong song) {
            if (song.getTimesPlayed() > 2 * averageSongsPlayed)
                mView.findViewById(R.id.popular_song_tag).setVisibility(View.VISIBLE);
            else
                mView.findViewById(R.id.popular_song_tag).setVisibility(View.INVISIBLE);
        }
    }
}
