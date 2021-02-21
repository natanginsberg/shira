package com.function.karaoke.interaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BlendMode;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.function.karaoke.interaction.activities.Model.DatabaseSong;
import com.function.karaoke.interaction.activities.Model.Recording;
import com.function.karaoke.interaction.activities.Model.Reocording;
import com.function.karaoke.interaction.fragments.SongsListFragment;
import com.function.karaoke.interaction.utils.static_classes.Converter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GridAdapter extends BaseAdapter {
    private static final int[] rectangles = new int[]{R.drawable.custom_song_rec_1,
            R.drawable.custom_song_rec_2, R.drawable.custom_song_rec_3, R.drawable.custom_song_rec_4};
    private static final int[] transparentRectangles = new int[]{R.drawable.custom_song_rec_1_t,
            R.drawable.custom_song_rec_2_t, R.drawable.custom_song_rec_3_t, R.drawable.custom_song_rec_4_t};
    private static final int[] layouts = new int[]{R.layout.song_display_big, R.layout.song_display_small};
    private static final double[] heightFactors = new double[]{2.5, 3.5};
    private static final Comparator<Reocording> mComparator = new Comparator<Reocording>() {
        @Override
        public int compare(Reocording a, Reocording b) {
            if (!a.getTitle().equalsIgnoreCase(b.getTitle()))
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            return a.getArtist().compareToIgnoreCase(b.getArtist());
        }
    };
    private final SongsListFragment.OnListFragmentInteractionListener mListener;
    private final Context context;
    private final LayoutInflater layoutInflator;
    private final List<DatabaseSong> mValues;
    private List<Recording> mRecordings;
    private final String language;
    private String text;

    public GridAdapter(Context c, List<DatabaseSong> songs, SongsListFragment.OnListFragmentInteractionListener listener, String language) {
        mListener = listener;
        this.language = language;
        mValues = songs;
        if (mValues != null)
            Collections.sort(mValues, mComparator);
        context = c;
        layoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mValues.size();
    }

    public void add(List<DatabaseSong> songs) {

        mValues.addAll(songs);
        Collections.sort(mValues, mComparator);
        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        return mValues.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View grid, ViewGroup parent) {
        ViewHolder viewHolder;
        if (grid == null) {
            int randomSize = Math.random() < 0.5 ? 0 : 1;
            int randomColor = Math.random() < 0.5 ? 0 : 1;
            grid = layoutInflator.inflate(layouts[randomSize], parent, false);
//            imageView = (ImageView) grid.findViewById(R.id.grid_item);
            setParams(grid, parent, randomSize, randomColor);
            viewHolder = new ViewHolder(grid);
            viewHolder.setItem(mValues.get(position));
            grid.setTag(viewHolder);
            grid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != mListener) {
                        mListener.onListFragmentInteractionPlay(viewHolder.mItem);
                    }
                }
            });

        } else {
            viewHolder = (ViewHolder) grid.getTag();
        }


        return grid;

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setParams(View view, View parent, int randomSize, int randomColor) {
        if (language.equals("iw"))
            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        else
            view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = ((int) (parent.getWidth() / 2.3));
        layoutParams.height = ((int) (parent.getHeight() / heightFactors[randomSize]));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.findViewById(R.id.song_placeholder).setBackground(context.getResources().getDrawable(rectangles[randomSize * 2 + randomColor], null));
            view.findViewById(R.id.song_placeholder).setBackgroundTintBlendMode(BlendMode.COLOR_DODGE);
        } else {
            view.findViewById(R.id.song_placeholder).setBackground(context.getResources().getDrawable(transparentRectangles[randomSize * 2 + randomColor]));
        }

    }

    private class ViewHolder {
        private final TextView mLblTitle;
        private final TextView mLblArtist;
        private final ShapeableImageView mCover;

        //        public Song mItem;
        public Reocording mItem;

        public ViewHolder(View view) {
            mLblTitle = view.findViewById(R.id.lbl_title);
            mLblArtist = view.findViewById(R.id.lbl_artist);
            mCover = view.findViewById(R.id.img_cover);
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


}