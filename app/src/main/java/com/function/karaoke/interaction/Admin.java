package com.function.karaoke.interaction;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.function.karaoke.interaction.activities.Model.FirestoreSong;
import com.function.karaoke.interaction.storage.DatabaseDriver;
import com.function.karaoke.interaction.storage.SongAdder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Admin extends AppCompatActivity {

    SongAdder songAdder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        songAdder = new SongAdder(new DatabaseDriver());

    }

    public void enterSong(View view) {
        String songName = ((EditText) findViewById(R.id.song_name)).getText().toString();
        String genres = ((EditText) findViewById(R.id.genre)).getText().toString();
        String artistName = ((EditText) findViewById(R.id.artist_name)).getText().toString();


        FirestoreSong firestoreSong = new FirestoreSong(songName, artistName, genres, new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(new Date()));
        songAdder.addSongToDatabase(firestoreSong);
    }
}