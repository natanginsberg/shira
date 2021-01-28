package com.function.karaoke.hardware;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.RecordingDB;
import com.function.karaoke.hardware.activities.Model.enums.RecordingsScreenState;
import com.function.karaoke.hardware.storage.RecordingDelete;
import com.function.karaoke.hardware.storage.RecordingService;
import com.function.karaoke.hardware.tasks.NetworkTasks;
import com.function.karaoke.hardware.utils.static_classes.Converter;
import com.function.karaoke.hardware.utils.static_classes.ShareLink;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecordingsList extends AppCompatActivity implements
        RecordingCategoryAdapter.RecordingSongListener, RecordingRecycleViewAdapter.RecordingListener {

    private static final int NUM_COLUMNS = 2;
    private RecordingService recordingService;
    private RecordingCategoryAdapter recordingCategoryAdapter;
    private RecyclerView recyclerView;
    private RecordingsScreenState recordingState;
    private RecordingDelete recordingDelete;
    private RecordingRecycleViewAdapter recordAdapter;
    private String previousQuery = "";
    private ArrayList<RecordingDB> previousRecordings = new ArrayList<>();
    private RecordingDB currentDatabaseRecordings;
    private boolean deleteOpen = false;
    private List<Recording> currentRecordings = new ArrayList<Recording>() {
        @Override
        public boolean contains(@Nullable Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public int indexOf(@Nullable Object o) {
            if (o != null) {
                if (o instanceof Recording) {
                    Recording rec = (Recording) o;
                    for (int i = 0; i < currentRecordings.size(); i++)
                        if (rec.getDate().equals(currentRecordings.get(i).getDate()))
                            return i;
                }
            }
            return -1;
        }
    };
    private List<Recording> deleteRecordingList = new ArrayList<Recording>() {
        @Override
        public boolean contains(@Nullable Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public int indexOf(@Nullable Object o) {
            if (o != null) {
                if (o instanceof Recording) {
                    Recording rec = (Recording) o;
                    for (int i = 0; i < deleteRecordingList.size(); i++)
                        if (rec.getDate().equals(deleteRecordingList.get(i).getDate()))
                            return i;
                }
            }
            return -1;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //todo pass and get user
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings_list);
        recordingService = new RecordingService();
        recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
        recordingState = RecordingsScreenState.RECORDING_SONGS_DISPLAYED;
        setRecordingsObserver();
        addSearchListener();
    }

    private void addSearchListener() {
        SearchView searchView = findViewById(R.id.search_input);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (recordingState == RecordingsScreenState.SINGLE_SONG_RECORDINGS)
                    onBackPressed();
                if (query.length() >= 1) {
                    if (query.length() > previousQuery.length()) {
                        addCopyOfSongsDBToList(currentDatabaseRecordings);
                        getSongsSearchedFor(query.toLowerCase());
                    } else {
                        currentDatabaseRecordings.updateRecordings(previousRecordings.get(previousRecordings.size() - 1).getRecordings());
                        previousRecordings.remove(previousRecordings.size() - 1);
                    }
                    recordingCategoryAdapter.setData(currentDatabaseRecordings.getRecordingsPerSong());
                    recordingCategoryAdapter.notifyDataSetChanged();
                    previousQuery = query;
                } else {
                    if (previousRecordings.size() != 0) {
                        currentDatabaseRecordings.updateRecordings(previousRecordings.get(0).getRecordings());
                        recordingCategoryAdapter.notifyDataSetChanged();
                        previousRecordings = new ArrayList<>();
                        previousQuery = "";
                    }
                }
                return false;
            }
        });
    }

    private void getSongsSearchedFor(String query) {
        List<Recording> searchedRecordings = new ArrayList<>();
        for (Recording recording : currentDatabaseRecordings.getRecordings()) {
            if (recording.getTitle().toLowerCase().contains(query) || recording.getArtist().toLowerCase().contains(query)) {
                searchedRecordings.add(recording);

            }
        }
        currentDatabaseRecordings.updateRecordings(searchedRecordings);
    }

    private void addCopyOfSongsDBToList(RecordingDB recordings) {
        previousRecordings.add(new RecordingDB(recordings.getRecordings()));
    }

    private void setRecordingsObserver() {
        final Observer<List<Recording>> personalRecordingObserver = personalRecordings -> {
            if (personalRecordings != null) {
                currentDatabaseRecordings = new RecordingDB(personalRecordings);
                findViewById(R.id.loading_songs_progress_bar).setVisibility(View.INVISIBLE);
                findViewById(R.id.no_recordings_text).setVisibility(View.INVISIBLE);
                displayRecordingSongs();
            } else
                findViewById(R.id.no_recordings_text).setVisibility(View.VISIBLE);
        };
        this.recordingService.getRecordingFromUID().observe(this, personalRecordingObserver);

    }

    private void displayRecordingSongs() {
        findViewById(R.id.song_info).setVisibility(View.GONE);
        recordingCategoryAdapter = new RecordingCategoryAdapter(currentDatabaseRecordings.getRecordingsPerSong(), this, getString(R.string.recording_tag_display_constant));
        recyclerView.setAdapter(recordingCategoryAdapter);
    }


    public void back(View view) {
        if (recordingState == RecordingsScreenState.RECORDING_SONGS_DISPLAYED)
            finish();
        else {
            recordingState = RecordingsScreenState.RECORDING_SONGS_DISPLAYED;
            recyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
            displayRecordingSongs();
        }
    }

    @Override
    public void onBackPressed() {
        if (recordingState == RecordingsScreenState.RECORDING_SONGS_DISPLAYED)
            finish();
        else {
            currentDatabaseRecordings.updateRecordings(currentRecordings);
            recordingState = RecordingsScreenState.RECORDING_SONGS_DISPLAYED;
            recyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
            displayRecordingSongs();
        }
    }

    @Override
    public void onListFragmentInteractionPlay(List<Recording> recordings) {
        recordingState = RecordingsScreenState.SINGLE_SONG_RECORDINGS;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currentRecordings = recordings;
        recordAdapter = new RecordingRecycleViewAdapter(recordings, this);
        recyclerView.setAdapter(recordAdapter);
        setSongInfo(recordings.get(0).getArtist(), recordings.get(0).getTitle(), recordings.get(0).getImageResourceFile());
        resetFields();

    }

    private void resetFields() {
        if (deleteRecordingList != null)
            deleteRecordingList.clear();
        changeMainIconBackToGray();
        deleteOpen = false;
    }

    private void setSongInfo(String artist, String title, String albumResource) {
        findViewById(R.id.song_info).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.artist_name)).setText(artist);
        ((TextView) findViewById(R.id.song_name)).setText(title);
        ShapeableImageView mCover = findViewById(R.id.recording_album_pic);
        if (!albumResource.equals("")) {
            Picasso.get()
                    .load(albumResource)
                    .placeholder(R.drawable.plain_rec)
                    .fit()
                    .into(mCover);
        }
        mCover.setShapeAppearanceModel(mCover.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, Converter.convertDpToPx(10))
                .build());
    }

    @Override
    public void onListFragmentInteractionPlay(Recording item) {
        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(SingActivity.RECORDING, item);
        startActivity(intent);
    }

    @Override
    public void onListFragmentInteractionShare(Recording item) {
        Task<ShortDynamicLink> link = ShareLink.createLink(item);
        link.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri shortLink = task.getResult().getShortLink();
                Uri flowchartLink = task.getResult().getPreviewLink();
                String link1 = shortLink.toString();
                sendDataThroughIntent(link1);
            } else {
                showFailure();
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void changeIconToWhite(View itemView) {
        itemView.findViewById(R.id.delete_button).setBackground(getDrawable(R.drawable.outline_circle));
        itemView.findViewById(R.id.trash).setBackground(getDrawable(R.drawable.ic_trash_icon_white));
    }

    @Override
    public void showAllGarbagesInChildren() {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            if (recyclerView.findViewHolderForAdapterPosition(i) != null) {
                recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
                recordAdapter.notifyItemChanged(i);
            }
        }
        recordAdapter.changeDeleteOpen(true);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void changeIconToGreen(View itemView) {
        itemView.findViewById(R.id.delete_button).setBackground(getDrawable(R.drawable.full_circle));
        itemView.findViewById(R.id.trash).setBackground(getDrawable(R.drawable.ic_trash_icon_grenn));
    }

    @Override
    public void deletePressed(Recording mItem, View itemView) {
        if (deleteRecordingList.contains(mItem)) {
            deleteRecordingList.remove(mItem);
            changeIconToWhite(itemView);
        } else {
            deleteRecordingList.add(mItem);
            changeIconToGreen(itemView);
        }
        recordAdapter.updateDeleteList(deleteRecordingList);
        changeMainIconToColor();
    }

    private void deleteRecordings() {
        NetworkTasks.deleteFromWasabi(recordingDelete, new NetworkTasks.DeleteListener() {
            @Override
            public void onSuccess() {
                showSuccessToast();
                recordAdapter.removeDeletions();
            }

            @Override
            public void onFail() {
            }
        });
    }

    private void showSuccessToast() {
        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
    }

    private void sendDataThroughIntent(String link) {
        String data = getString(R.string.email_prompt) + link;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(
                Intent.EXTRA_TEXT, data);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void showFailure() {
        Toast.makeText(this, getString(R.string.sharing_failed), Toast.LENGTH_SHORT).show();
    }

    public void deleteOption(View view) {
        if (deleteOpen) {
            deleteAllClickedRecordings();
            changeMainIconBackToGray();
            closeAllOpenGarbages();

        } else {
            showAllGarbagesInChildren();
            changeMainIconToColor();
        }
        deleteOpen = !deleteOpen;

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void changeMainIconToColor() {
        findViewById(R.id.main_delete_button).setBackground(getDrawable(R.drawable.full_circle_blue));
        findViewById(R.id.main_trash).setBackground(getDrawable(R.drawable.ic_trash_icon_white));
    }

    private void closeAllOpenGarbages() {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            if (recyclerView.findViewHolderForAdapterPosition(i) != null) {
                Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.delete_button).setVisibility(View.GONE);
                recordAdapter.notifyItemChanged(i);
            }
        }
        recordAdapter.changeDeleteOpen(false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void changeMainIconBackToGray() {
        findViewById(R.id.main_delete_button).setBackground(getDrawable(R.drawable.outline_circle_grey));
        findViewById(R.id.main_trash).setBackground(getDrawable(R.drawable.ic_trash_icon_grey));
    }

    private void deleteAllClickedRecordings() {
        if (deleteRecordingList.size() > 0) {
            recordAdapter.setRemoveInProgress();
            recordingDelete = new RecordingDelete(new RecordingDelete.SetupListener() {
                @Override
                public void setup() {
                    deleteRecordings();
                }
            }, deleteRecordingList);
            deleteRecordingsFromList();
        }
    }

    private void deleteRecordingsFromList() {
        currentRecordings.removeAll(deleteRecordingList);
        currentDatabaseRecordings.removeDeletedRecordings(deleteRecordingList);
        deleteRecordingList.clear();
    }
}