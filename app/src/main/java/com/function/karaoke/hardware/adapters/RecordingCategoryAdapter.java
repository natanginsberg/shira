package com.function.karaoke.hardware.adapters;

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
import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.Reocording;
import com.function.karaoke.hardware.fragments.SongsListFragment.OnListFragmentInteractionListener;
import com.function.karaoke.hardware.utils.static_classes.Converter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class RecordingCategoryAdapter extends RecyclerView.Adapter<RecordingCategoryAdapter.ViewHolder> {

    private static final Comparator<Reocording> mComparator = new Comparator<Reocording>() {
        @Override
        public int compare(Reocording a, Reocording b) {
            if (!a.getTitle().equalsIgnoreCase(b.getTitle()))
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            return a.getArtist().compareToIgnoreCase(b.getArtist());
        }
    };

    private List<Reocording> mValues;
    private Map<Reocording, List<Recording>> reocordingListMap;
    private List<Recording> mRecordings;
    private final RecordingSongListener mListener;
    private String recordingsWord;
    private static final int[] rectangles = new int[]{R.drawable.custom_song_rec_1,
            R.drawable.custom_song_rec_2, R.drawable.custom_song_rec_3, R.drawable.custom_song_rec_4};
    private static final int[] transparentRectangles = new int[]{R.drawable.custom_song_rec_1_t,
            R.drawable.custom_song_rec_2_t, R.drawable.custom_song_rec_3_t, R.drawable.custom_song_rec_4_t};
    private static final int[] layouts = new int[]{R.layout.song_display_big, R.layout.song_display_small};
    private static final double[] heightFactors = new double[]{2.5, 3.5};

    //    public SongRecyclerViewAdapter(List<Song> items, OnListFragmentInteractionListener listener, String language) {
//        setData(items);
//        mListener = listener;
//        this.language = language;
//    }
    public RecordingCategoryAdapter(Map<Reocording, List<Recording>> reocordingListMap, RecordingSongListener listener, String recordingsWord) {
        setData(reocordingListMap);
        mListener = listener;
        this.recordingsWord = recordingsWord;
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
                .inflate(R.layout.recording_song_display_big, parent, false);
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
//        switch (viewType){
//            case 0:
//                return new ViewHolderBig(view);
//            case 1:
//                return new ViewHolderSmall(view);
//        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
//        switch (holder.getItemViewType()){
//            case 0:
//                ViewHolderBig bigHolder = (ViewHolderBig) holder;
        holder.setItem(mValues.get(position));

        // making the click only on the button and not on the whole icon

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mListener) {
                    mListener.onListFragmentInteractionPlay(reocordingListMap.get(holder.mItem));
                }
            }
        });
//                break;
//            case 1:
//                ViewHolderSmall smallHolder = (ViewHolderSmall) holder;
//                smallHolder.setItem(mValues.get(position));
//
//                 making the click only on the button and not on the whole icon
//
//                smallHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (null != mListener) {
//                            mListener.onListFragmentInteractionRecordingClick(holder.mItem);
//                        }
//                    }
//                });
//        }

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(Map<Reocording, List<Recording>> songs) {
        mValues = new ArrayList<>(songs.keySet());// make a copy
        reocordingListMap = songs;
        Collections.sort(mValues, mComparator);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mLblTitle;
        private final TextView mLblArtist;
        private final ShapeableImageView mCover;
        private final TextView mRecordingAmount;

        //        public Song mItem;
        public Reocording mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLblTitle = view.findViewById(R.id.lbl_title);
            mLblArtist = view.findViewById(R.id.lbl_artist);
            mCover = view.findViewById(R.id.img_cover);
            mRecordingAmount = view.findViewById(R.id.number_of_recordings);
//            ((TextView) view.findViewById(R.id.play_button)).setText(text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLblArtist.getText() + "'";
        }

        public void setItem(Reocording song) {
            mItem = song;
            mLblTitle.setText(song.getTitle());
            mLblArtist.setText(song.getArtist());
            String numberOfSongs = (reocordingListMap.get(song) == null ? 0 : reocordingListMap.get(song).size()) + recordingsWord;
            mRecordingAmount.setText(numberOfSongs);
//            if (!language.equals("English")) {
            if (!song.getImageResourceFile().equals("")) {
                Picasso.get()
                        .load(song.getImageResourceFile())
                        .placeholder(R.drawable.plain_rec)
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

    public interface RecordingSongListener {

        void onListFragmentInteractionPlay(List<Recording> recordings);
    }

}
