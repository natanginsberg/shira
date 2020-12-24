package com.function.karaoke.hardware;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.function.karaoke.hardware.activities.Model.FirestoreSong;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.SongAdder;

public class Admin extends AppCompatActivity {

    private String songName;
    private String artistName;
    private String genres;
    SongAdder songAdder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        songAdder = new SongAdder(new DatabaseDriver());

            }

    public void enterSong(View view) {
        songName = (String) ((EditText) findViewById(R.id.song_name)).getText().toString();
        genres = (String) ((EditText) findViewById(R.id.genre)).getText().toString();
        artistName = (String) ((EditText) findViewById(R.id.artist_name)).getText().toString();

        FirestoreSong firestoreSong = new FirestoreSong(songName, artistName, genres);
        songAdder.addSongToDatabase(firestoreSong);
    }
}