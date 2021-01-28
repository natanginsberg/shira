package com.function.karaoke.hardware;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.core.model.Song;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.fragments.SongsListFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class RecordingRecycleViewAdapter extends RecyclerView.Adapter<RecordingRecycleViewAdapter.ViewHolder> {

    // there can be other sort strategies and filters
//    private static final Comparator<Song> mComparator = new Comparator<Song>() {
//        @Override
//        public int compare(Song a, Song b) {
//            if (!a.artist.equalsIgnoreCase(b.artist))
//                return a.artist.compareToIgnoreCase(b.artist);
//            return a.title.compareToIgnoreCase(b.title);
//        }
//    };

    private static final Comparator<Recording> mComparator = new Comparator<Recording>() {
        @Override
        public int compare(Recording a, Recording b) {
            if (!a.getDate().split("_")[0].equalsIgnoreCase(b.getDate().split("_")[0]))
                return b.getDate().compareToIgnoreCase(a.getDate());
            if (!a.getTitle().equalsIgnoreCase(b.getTitle()))
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            return a.getArtist().compareToIgnoreCase(b.getArtist());
        }
    };
    private final RecordingListener mListener;
    private List<Recording> mValues = new ArrayList<Recording>() {
        @Override
        public boolean contains(@Nullable Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public int indexOf(@Nullable Object o) {
            if (o != null) {
                if (o instanceof Recording) {
                    Recording rec = (Recording) o;
                    for (int i = 0; i < mValues.size(); i++)
                        if (rec.getDate().equals(mValues.get(i).getDate()))
                            return i;
                }
            }
            return -1;
        }
    };
    private boolean removeInProgress = false;
    private List<Integer> itemsToDelete = new ArrayList<>();
    private List<Recording> recordingsToDelete = new ArrayList<Recording>() {
        @Override
        public boolean contains(@Nullable Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public int indexOf(@Nullable Object o) {
            if (o != null) {
                if (o instanceof Recording) {
                    Recording rec = (Recording) o;
                    for (int i = 0; i < recordingsToDelete.size(); i++)
                        if (rec.getDate().equals(recordingsToDelete.get(i).getDate()))
                            return i;
                }
            }
            return -1;
        }
    };
    private boolean deleteOpen = false;

    //    public SongRecyclerViewAdapter(List<Song> items, OnListFragmentInteractionListener listener, String language) {
//        setData(items);
//        mListener = listener;
//        this.language = language;
//    }
    public RecordingRecycleViewAdapter(List<Recording> items, RecordingListener listener) {
        setData(items);
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recording_display_item, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (parent.getHeight() / 8);
        view.setLayoutParams(layoutParams);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setItem(mValues.get(position));
        if (deleteOpen)
            holder.mView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
        else
            holder.mView.findViewById(R.id.delete_button).setVisibility(View.GONE);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mListener.showAllGarbagesInChildren();
                mListener.deletePressed(holder.mItem, holder.itemView);
//                mListener.changeIconToGreen(holder.itemView);
//                itemsToDelete.add(position);
                return false;
            }
        });

        // making the click only on the button and not on the whole icon

        holder.itemView.findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mListener) {
                    mListener.onListFragmentInteractionPlay(holder.mItem);
                }
            }
        });
        holder.itemView.findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mListener) {
                    mListener.onListFragmentInteractionShare(holder.mItem);
                }
            }
        });
        holder.itemView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mListener) {
                    if (!removeInProgress) {
//                        removeInProgress = true;
                        mListener.deletePressed(holder.mItem, holder.mView);

                    }
                }
            }
        });
    }

    public void removeAt(int i) {
        mValues.remove(i);
        notifyItemRemoved(i);
        notifyItemRangeChanged(i, mValues.size());

    }

    public List<Integer> getItemsToDelete() {
        return itemsToDelete;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(List<Recording> songs) {
        mValues = new ArrayList<>(songs); // make a copy
        Collections.sort(mValues, mComparator);
    }

    public void removeDeletions() {
        mValues.removeAll(recordingsToDelete);
        notifyDataSetChanged();
        recordingsToDelete.clear();
        removeInProgress = false;
    }

    public void setRemoveInProgress() {
        removeInProgress = true;
    }

    public void changeDeleteOpen(boolean deleteOpen) {
        this.deleteOpen = deleteOpen;
        notifyDataSetChanged();
    }

    public void updateDeleteList(List<Recording> deleteRecordingList) {
        recordingsToDelete.clear();
        recordingsToDelete.addAll(deleteRecordingList);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mLblTitle;
        private final TextView mDate;

        public Recording mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLblTitle = view.findViewById(R.id.custom_song_title);
            mDate = view.findViewById(R.id.date_recorded);

            if (deleteOpen)
                view.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
            else
                view.findViewById(R.id.delete_button).setVisibility(View.GONE);

        }

        @Override
        public String toString() {
            return super.toString() + " '";
        }

        public void setItem(Recording song) {
            if (recordingsToDelete.contains(song))
                mListener.changeIconToGreen(mView);
            else
                mListener.changeIconToWhite(mView);
            mItem = song;
            mLblTitle.setText(song.getTitle());
            String date = getDateAndTime(song.getDate());
            mDate.setText(date);
            if (song.isLoading())
                ((TextView) mView.findViewById(R.id.share_button)).setBackgroundColor(Color.GRAY);
        }

        private String getDateAndTime(String date) {
            return date.substring(9, 11) + ":" + date.substring(11, 13) + "  " + date.substring(6, 8) + "/" + date.substring(4, 6) + "/" + date.substring(2, 4);
        }
    }

    public interface RecordingListener {

        void onListFragmentInteractionPlay(Recording mItem);

        void onListFragmentInteractionShare(Recording mItem);

        void showAllGarbagesInChildren();

        void changeIconToGreen(View itemView);

        void deletePressed(Recording mItem, View itemView);

        void changeIconToWhite(View mView);
    }

}

