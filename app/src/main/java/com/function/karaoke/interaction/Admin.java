package com.function.karaoke.interaction;

import android.Manifest;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.function.karaoke.interaction.activities.Model.DatabaseSong;
import com.function.karaoke.interaction.activities.Model.FirestoreSong;
import com.function.karaoke.interaction.activities.Model.InternetUser;
import com.function.karaoke.interaction.activities.Model.Recording;
import com.function.karaoke.interaction.activities.Model.SongRequest;
import com.function.karaoke.interaction.storage.DatabaseDriver;
import com.function.karaoke.interaction.storage.InternetUserDatabase;
import com.function.karaoke.interaction.storage.SongAdder;
import com.function.karaoke.interaction.storage.SongRequests;
import com.function.karaoke.interaction.storage.SongService;
import com.function.karaoke.interaction.ui.IndicationPopups;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Admin extends AppCompatActivity {

    SongAdder songAdder;
    SongService songService;
    InternetUserDatabase.AddListener addListener;
    private int playCount = 0;
    private int downloadCount = 0;
    private StringBuilder songsNeverPlayed = new StringBuilder();
    private int songsNeverPlayedConter;
    Set<String> users = new HashSet<>();
    int savedSongs = 0;
    private Set<String> titles = new HashSet<>();
    private StringBuilder songsNeverEverPlayed = new StringBuilder();
    private int songsNeverEverPlayedConter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                100);
        setContentView(R.layout.activity_admin);
        songAdder = new SongAdder(new DatabaseDriver());
        songService = new SongService();

        addListener = new InternetUserDatabase.AddListener() {
            @Override
            public void onSuccess() {
                showRequestGranted();
            }

            @Override
            public void onFail() {
                showRequestDenied();
            }

            @Override
            public void nameExists() {
                showNameExistsAlready();
            }
        };

    }

    private void showNameExistsAlready() {
        PopupWindow popupWindow = IndicationPopups.openXIndication(this, findViewById(R.id.admin_screen), getResources().
                getString(R.string.name_exists));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        }, 1000);
    }

    public void showRequestDenied() {
        PopupWindow popupWindow = IndicationPopups.openXIndication(this, findViewById(R.id.admin_screen), getResources().
                getString(R.string.failed));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        }, 1000);
    }

    public void showRequestGranted() {
        PopupWindow popupWindow = IndicationPopups.openCheckIndication(this, findViewById(R.id.admin_screen), getResources().
                getString(R.string.success));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        }, 1000);
    }


    public void enterSong(View view) {
        String songName = ((EditText) findViewById(R.id.song_name)).getText().toString();
        String genres = ((EditText) findViewById(R.id.genre)).getText().toString();
        String artistName = ((EditText) findViewById(R.id.artist_name)).getText().toString();


        FirestoreSong firestoreSong = new FirestoreSong(songName, artistName, genres, new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(new Date()));
        songAdder.addSongToDatabase(firestoreSong);
    }

    public void downloadAllRequests(View view) {
        SongRequests.getAllRequestedSongs(new DatabaseDriver(), new SongRequests.RequestGetter() {
            @Override
            public void getSongs(List<SongRequest> songs) {
                downloadAllSongsToPhone(songs);
            }
        });
    }

    private void downloadAllSongsToPhone(List<SongRequest> songs) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Folder path

        File myFile = new File(path, "requests.doc");
//        try {
//            myFile.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(myFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

        for (int i = 0; i < songs.size(); i++) {
            try {
                myOutWriter.append("שם השיר:");
                myOutWriter.append(songs.get(i).getTitle());
                myOutWriter.append("  זמר:");
                myOutWriter.append(songs.get(i).getArtist());
                myOutWriter.append("  הערות:");
                myOutWriter.append(songs.get(i).getComments());
                myOutWriter.append("\n\r");
                myOutWriter.append("\n\r");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        try {
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getBaseContext(),
                "Success'",
                Toast.LENGTH_SHORT).show();
    }


    private void getSongLength(DatabaseSong song) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(song.getSongResourceFile());
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    songService.addSongTime(song.getTitle(), mediaPlayer.getDuration());
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addLength(View view) {
        String songName = ((EditText) findViewById(R.id.song_name)).getText().toString();
        if (songName.equals(""))
            (findViewById(R.id.song_name)).setBackgroundColor(Color.RED);
        else
            getSong(((EditText) findViewById(R.id.song_name)).getText().toString());
    }

    private void getSong(String title) {
        DatabaseDriver db = new DatabaseDriver();
        db.getSong(title, new DatabaseDriver.SongListener() {
            @Override
            public void onSuccess(List<DatabaseSong> songs) {
                if (songs.size() > 0)
                    for (DatabaseSong song : songs)
                        getSongLength(song);
                else
                    (findViewById(R.id.song_name)).setBackgroundColor(Color.RED);
            }

            @Override
            public void onFail() {

            }
        });
    }

    public void addUser(View view) {
        String userName = ((EditText) findViewById(R.id.user_name)).getText().toString();
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String date = ((EditText) findViewById(R.id.date)).getText().toString();

        if (userName.equals("") || email.equals("")) {
            if (userName.equals("")) {
                findViewById(R.id.user_name).setBackgroundColor(Color.RED);
            }
            if (email.equals("")) {
                findViewById(R.id.email).setBackgroundColor(Color.RED);
            }
        } else {
            findViewById(R.id.email).setBackgroundColor(Color.BLACK);
            findViewById(R.id.user_name).setBackgroundColor(Color.BLACK);
            InternetUser internetUser = new InternetUser(userName, email, date);
            InternetUserDatabase.addUserToDatabase(internetUser, addListener);
        }
    }

    public void deleteUser(View view) {
        String userName = ((EditText) findViewById(R.id.user_name)).getText().toString();

        if (userName.equals("")) {
            findViewById(R.id.user_name).setBackgroundColor(Color.RED);
        } else {
            findViewById(R.id.email).setBackgroundColor(Color.BLACK);
            findViewById(R.id.user_name).setBackgroundColor(Color.BLACK);
            InternetUserDatabase.deleteUserFromDatabase(userName, addListener);
        }
    }

    public void changeEmail(View view) {
        String userName = ((EditText) findViewById(R.id.user_name)).getText().toString();
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String date = ((EditText) findViewById(R.id.date)).getText().toString();

        if (userName.equals("") || email.equals("")) {
            if (userName.equals("")) {
                findViewById(R.id.user_name).setBackgroundColor(Color.RED);
            }
            if (email.equals("")) {
                findViewById(R.id.email).setBackgroundColor(Color.RED);
            }
        } else {
            findViewById(R.id.email).setBackgroundColor(Color.BLACK);
            findViewById(R.id.user_name).setBackgroundColor(Color.BLACK);
            InternetUser internetUser = new InternetUser(userName, email, date);
            InternetUserDatabase.editUserInDatabase(internetUser, addListener);
        }
    }

    public void openSongAddition(View view) {
        findViewById(R.id.song_addition).setVisibility(View.VISIBLE);
        findViewById(R.id.internet_user).setVisibility(View.GONE);
    }

    public void openUserAddition(View view) {
        findViewById(R.id.song_addition).setVisibility(View.GONE);
        findViewById(R.id.internet_user).setVisibility(View.VISIBLE);
    }

    public void getAllUsers(View view) {
        InternetUserDatabase.getAllSongsInCollection(new InternetUserDatabase.InternetUserListener() {
            @Override
            public void onSuccess(List<InternetUser> users) {
                showAllUsers(users);
            }

            @Override
            public void onFail() {
                showRequestDenied();
            }
        });
    }

    private void showAllUsers(List<InternetUser> users) {
        LinearLayout linearLayout = findViewById(R.id.all_users);
        for (InternetUser user : users) {
            Button textView = new Button(this);
            textView.setText(user.getName());
            textView.setTextColor(Color.BLUE);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((EditText) findViewById(R.id.user_name)).setText(user.getName());
                    ((EditText) findViewById(R.id.email)).setText(user.getEmail());

                }
            });
            linearLayout.addView(textView);
        }
    }

    public void getSongs(View view) {
        final boolean[] started = {false};
        DatabaseDriver databaseDriver = new DatabaseDriver();
        databaseDriver.getAllSongsInCollection(new DatabaseDriver.SongListener() {
            @Override
            public void onSuccess(List<DatabaseSong> songs) {
                if (started[0])
                    return;
                started[0] = true;
                for (DatabaseSong ds : songs) {
                    playCount += ds.getTimesPlayed();
                    downloadCount += ds.getTimesDownloaded();
                    if (ds.getTimesPlayed() < 5) {
                        songsNeverPlayed.append(ds.getTitle());
                        songsNeverPlayed.append("\n");
                        songsNeverPlayedConter += 1;
                        if (ds.getTimesPlayed() == 0) {
                            songsNeverEverPlayed.append(ds.getTitle());
                            songsNeverEverPlayedConter += 1;
                        }
                    }
                }
                showSongData();
            }

            @Override
            public void onFail() {

            }
        });
    }

    private void showSongData() {
        String textToShow = "Songs played in total: " + playCount + ". Total downloads: " +
                downloadCount + ". " + "Songs less than 5 times played: " + songsNeverPlayedConter +
                ". Songs are " + songsNeverPlayed.toString() + ". Songs never ever played: " +
                songsNeverEverPlayed.toString() + ". And the amount is: " + songsNeverEverPlayedConter;

        System.out.println(textToShow);
    }

    public void getRecordings(View view) {
        final boolean[] started = {false};
        DatabaseDriver databaseDriver = new DatabaseDriver();
        databaseDriver.getAllRecordings(new DatabaseDriver.RecordingL() {
            @Override
            public void onSuccess(List<Recording> recordings) {
                if (started[0])
                    return;
                started[0] = true;
                savedSongs = recordings.size();
                for (Recording recording : recordings) {
                    users.add(recording.getRecorderId());
                    titles.add(recording.getTitle());
                }
                showRecordingData();
            }
        });
    }

    private void showRecordingData() {
        String textToShow = "Recordings in the system total: " + savedSongs + ". Total users to save songs: " +
                users.size() + ". " + "Number of songs downloaded: " + titles.size() + ". ";

        System.out.println(textToShow);
    }

}