package com.function.karaoke.interaction.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.function.karaoke.interaction.R;
import com.function.karaoke.interaction.activities.Model.Genres;
import com.function.karaoke.interaction.utils.static_classes.Converter;

import java.lang.ref.WeakReference;

public class GenresUI {

    private static final int GENRE = -1;
    private static final int MY_RECORDINGS = 101;
    private static final int SONG_SUGGESTION = 102;
    private final View view;
//    private final Context context;
    private final String currentLanguage;
    private final GenreUIListener gListener;
    private PopupWindow genrePopup;
    private String myRecording;
    private final WeakReference<Activity> activityWeakReference;

    public GenresUI(View view, Activity activity, String currentLanguage, GenreUIListener genreListener) {
        this.view = view;
//        this.context = context;
        activityWeakReference = new WeakReference<>(activity);
        this.currentLanguage = currentLanguage;
        this.gListener = genreListener;
    }

    public void addGenresToScreen(Genres genres, String currentGenre, int displayed) {

        ConstraintLayout viewGroup = view.findViewById(R.id.all_genres);
        LayoutInflater layoutInflater = (LayoutInflater) activityWeakReference.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View genreView = layoutInflater.inflate(R.layout.genre_dopdown_layout, viewGroup);
        addViewsToLinearLayout(genreView, genres, currentGenre, displayed);
        placeGenresOnScreen(genreView);
//        applyDim();
//        return genreView;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void placeGenresOnScreen(View genreView) {
        genrePopup = new PopupWindow(activityWeakReference.get());
        genrePopup.setContentView(genreView);
        genrePopup.setFocusable(true);
        genrePopup.setBackgroundDrawable(activityWeakReference.get().getResources().getDrawable(R.drawable.unclicked_recording_background));
        genrePopup.setWidth((int) (view.getWidth() * 0.3));
        genrePopup.showAtLocation(genreView, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0);
    }

    public void addViewsToLinearLayout(View gView, Genres genres, String currentGenre, int displayed) {
        LinearLayout linearLayout = gView.findViewById(R.id.genre_list);
        linearLayout.removeAllViews();
        for (int i = 0; i < genres.getSize(); i++) {
            String genre = genres.getGenres().get(i);
            TextView genreTextView = createGenreTextView(genre, i);
            if (displayed == GENRE && currentGenre.contains(genreTextView.getText().toString()))
                linearLayout.addView(genreTextView, 0);
            else
                linearLayout.addView(genreTextView);

        }
        if (displayed == MY_RECORDINGS)
            linearLayout.addView(addMyRecordingsOption(), 0);
        else
            linearLayout.addView(addMyRecordingsOption());
        if (displayed == SONG_SUGGESTION)
            linearLayout.addView(addSongSuggestionOption(), 0);
        else
            linearLayout.addView(addSongSuggestionOption());

    }

    private TextView createGenreTextView(String genre, int i) {
        TextView textView = new TextView(activityWeakReference.get());
        textView.setTextColor(Color.WHITE);

        setTextViewAttributes(textView);
        String genreToDisplay;
        if (genre.contains(","))
            if (currentLanguage.equals("en")) {
                genreToDisplay = genre.split(",")[0];
            } else {
                genreToDisplay = genre.split(",")[1];
            }
        else
            genreToDisplay = genre;
        textView.setText(genreToDisplay);

        textView.setOnClickListener(view -> {
            if (i >= 0) {
                gListener.getAllSongsFromGenre(i);
                addGenreToScreen(genreToDisplay);
            }
            closeGenreChoices();

        });
        return textView;
    }

    private void setTextViewAttributes(TextView textView) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                ((int) LinearLayout.LayoutParams.WRAP_CONTENT, (int) LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = Converter.convertDpToPx(10);
        params.bottomMargin = Converter.convertDpToPx(10);
        params.gravity = Gravity.CENTER;
        textView.setLayoutParams(params);
        textView.setHeight(Converter.convertDpToPx(24));
        Typeface tf = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/varela_round_regular.ttf");
        textView.setTypeface(tf);
    }

    private TextView addMyRecordingsOption() {
        TextView textView = new TextView(activityWeakReference.get());
        textView.setTextColor(Color.WHITE);

        setTextViewAttributes(textView);
        String textToDisplay = activityWeakReference.get().getResources().getString(R.string.my_recordings);
        textView.setText(textToDisplay);

        textView.setOnClickListener(view -> {
            gListener.openMyRecordings();
            closeGenreChoices();

        });
        return textView;
    }

    private TextView addSongSuggestionOption() {
        TextView textView = new TextView(activityWeakReference.get());
        textView.setTextColor(activityWeakReference.get().getResources().getColor(R.color.pick_a_song_color));

        setTextViewAttributes(textView);
        String textToDisplay = activityWeakReference.get().getResources().getString(R.string.song_suggestion);
        textView.setText(textToDisplay);

        textView.setOnClickListener(view -> {
            gListener.showSongSuggestionBox();
            closeGenreChoices();

        });
        return textView;
    }

    private void closeGenreChoices() {
        genrePopup.dismiss();
    }

    public void addGenreToScreen(String genre) {
        String genreToDisplay;
        if (genre.contains(","))
            if (currentLanguage.equals("en")) {
                genreToDisplay = genre.split(",")[0];
            } else {
                genreToDisplay = genre.split(",")[1];
            }
        else
            genreToDisplay = genre;
        TextView display = (TextView) view.findViewById(R.id.genre);
        display.setText(genreToDisplay);
        if (genreToDisplay.equalsIgnoreCase(activityWeakReference.get().getResources().getString(R.string.song_suggestion)))
            display.setTextColor(activityWeakReference.get().getResources().getColor(R.color.pick_a_song_color));
        else
            display.setTextColor(activityWeakReference.get().getResources().getColor(R.color.default_text_color));
    }


    public interface GenreUIListener {

        void getAllSongsFromGenre(int i);

        void showSongSuggestionBox();

        void openMyRecordings();
    }
}
