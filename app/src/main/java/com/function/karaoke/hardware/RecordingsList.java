package com.function.karaoke.hardware;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.RecordingDB;
import com.function.karaoke.hardware.activities.Model.enums.RecordingsScreenState;
import com.function.karaoke.hardware.fragments.SongsListFragment;
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

public class RecordingsList extends AppCompatActivity implements
        RecordingCategoryAdapter.RecordingSongListener, RecordingRecycleViewAdapter.RecordingListener {

    private static final int NUM_COLUMNS = 2;
    private RecordingDB recordingDB;
    private RecordingService recordingService;
    private RecordingCategoryAdapter recordingCategoryAdapter;
    private RecyclerView recyclerView;
    private RecordingsScreenState recordingState;
    private RecordingDelete recordingDelete;
    private RecordingRecycleViewAdapter recordAdapter;
    private String previousQuery = "";
    private ArrayList<RecordingDB> previousRecordings = new ArrayList<>();
    private RecordingDB currentDatabaseRecordings;

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
        findViewById(R.id.song_info).setVisibility(View.GONE);
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
//                        gAdapter.notifyDataSetChanged();
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
                recordingDB = new RecordingDB(personalRecordings);
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
            recordingState = RecordingsScreenState.RECORDING_SONGS_DISPLAYED;
            recyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
            displayRecordingSongs();
        }
    }

    @Override
    public void onListFragmentInteractionPlay(List<Recording> recordings) {
        recordingState = RecordingsScreenState.SINGLE_SONG_RECORDINGS;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recordAdapter = new RecordingRecycleViewAdapter(recordings, this);
        recyclerView.setAdapter(recordAdapter);
        setSongInfo(recordings.get(0).getArtist(), recordings.get(0).getTitle(), recordings.get(0).getImageResourceFile());

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

    @Override
    public void onListFragmentInteractionDelete(Recording mItem) {
        recordingDelete = new RecordingDelete(new RecordingDelete.SetupListener() {
            @Override
            public void setup() {
                deleteRecording();
            }
        }, mItem);
    }

    private void deleteRecording() {
        NetworkTasks.deleteFromWasabi(recordingDelete, new NetworkTasks.DeleteListener() {
            @Override
            public void onSuccess() {
                showSuccessToast();
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                SongsListFragment fragment = (SongsListFragment) fragments.get(0);
                fragment.removeRecording();
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
}