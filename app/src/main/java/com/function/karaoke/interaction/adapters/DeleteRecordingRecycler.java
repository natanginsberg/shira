package com.function.karaoke.interaction.adapters;

import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.core.model.Song;
import com.function.karaoke.interaction.fragments.SongsListFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * this recycler displays the songs recorded if the user were to have too many
 */
public class DeleteRecordingRecycler
//        extends RecyclerView.Adapter<DeleteRecordingRecycler.ViewHolder>
{
//
//    // there can be other sort strategies and filters
////    private static final Comparator<Song> mComparator = new Comparator<Song>() {
////        @Override
////        public int compare(Song a, Song b) {
////            if (!a.artist.equalsIgnoreCase(b.artist))
////                return a.artist.compareToIgnoreCase(b.artist);
////            return a.title.compareToIgnoreCase(b.title);
////        }
////    };
//
//    private static final Comparator<Recording> mComparator = new Comparator<Recording>() {
//        @Override
//        public int compare(Recording a, Recording b) {
//            if (!a.getDate().split("_")[0].equalsIgnoreCase(b.getDate().split("_")[0]))
//                return b.getDate().compareToIgnoreCase(a.getDate());
//            if (!a.getTitle().equalsIgnoreCase(b.getTitle()))
//                return a.getTitle().compareToIgnoreCase(b.getTitle());
//            return a.getArtist().compareToIgnoreCase(b.getArtist());
//        }
//    };
//    private final SingActivity.DeleteRecordingListener mListener;
//    private final String language;
//    private List<Recording> mValues;
//    private boolean removeInProgress = false;
//
//    //    public SongRecyclerViewAdapter(List<Song> items, OnListFragmentInteractionListener listener, String language) {
////        setData(items);
////        mListener = listener;
////        this.language = language;
////    }
//    public DeleteRecordingRecycler(List<Recording> items, SingActivity.DeleteRecordingListener listener, String language) {
//        setData(items);
//        mListener = listener;
//        this.language = language;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view;
//        view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.recording_delete_item, parent, false);
//        if (language.equals("iw"))
//            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
//        else
//            view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        layoutParams.height = (parent.getHeight() / 4);
//        view.setLayoutParams(layoutParams);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
//
//        holder.setItem(mValues.get(position));
//
//        // making the click only on the button and not on the whole icon
//
//        holder.itemView.findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (null != mListener) {
//                    mListener.play(holder.mItem);
//                }
//            }
//        });
//        holder.itemView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (null != mListener) {
//                    if (!removeInProgress) {
//                        removeInProgress = true;
//                        mListener.delete(holder.mItem);
//                    }
//                }
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return mValues.size();
//    }
//
//    public void setData(List<Recording> songs) {
//        mValues = new ArrayList<>(songs); // make a copy
//        Collections.sort(mValues, mComparator);
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        private final View mView;
//        private final TextView mLblTitle;
//        private final TextView mLblArtist;
//        private final ImageView mCover;
//        private final TextView mDate;
//
//        public Recording mItem;
//
//        public ViewHolder(View view) {
//            super(view);
//            mView = view;
//            mLblTitle = view.findViewById(R.id.lbl_title);
//            mLblArtist = view.findViewById(R.id.lbl_artist);
//            mCover = view.findViewById(R.id.img_cover);
//            mDate = view.findViewById(R.id.date_recorded);
//        }
//
//        @Override
//        public String toString() {
//            return super.toString() + " '" + mLblArtist.getText() + "'";
//        }
//
//        public void setItem(Recording song) {
//            mItem = song;
//            mLblTitle.setText(song.getTitle());
//            mLblArtist.setText(song.getArtist());
//            String date = manipulateDateToBePretty(song.getDate());
//            mDate.setText(date);
//            Typeface tf = Typeface.createFromAsset(mView.getContext().getAssets(), "fonts/SecularOne_Regular.ttf");
//            mLblTitle.setTypeface(tf);
//            mLblArtist.setTypeface(tf);
//            ((TextView) mView.findViewById(R.id.play_button)).setTypeface(tf);
//
//            mDate.setTypeface(tf);
//            Picasso.get()
//                    .load(song.getImageResourceFile())
//                    .placeholder(R.drawable.plain_rec)
//                    .fit()
//                    .into(mCover);
//        }
//
//        private String manipulateDateToBePretty(String date) {
//            return date.substring(6, 8) + "/" + date.substring(4, 6) + "/" + date.substring(2, 4);
//        }
//    }

}

