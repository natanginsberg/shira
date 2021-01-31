package com.function.karaoke.hardware.ui;

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

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.Genres;
import com.function.karaoke.hardware.utils.static_classes.Converter;

public class GenresUI {

    private static final int MY_RECORDINGS = 101;
    private static final int SONG_SUGGESTION = 102;
    private final View view;
    private final Context context;
    private final String currentLanguage;
    private final GenreUIListener gListener;
    private PopupWindow genrePopup;
    private String myRecording;

    public GenresUI(View view, Context context, String currentLanguage, GenreUIListener genreListener) {
        this.view = view;
        this.context = context;
        this.currentLanguage = currentLanguage;
        this.gListener = genreListener;
    }

    public void addGenresToScreen(Genres genres, String currentGenre, int displayed) {

        ConstraintLayout viewGroup = view.findViewById(R.id.all_genres);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View genreView = layoutInflater.inflate(R.layout.genre_dopdown_layout, viewGroup);
        addViewsToLinearLayout(genreView, genres, currentGenre, displayed);
        placeGenresOnScreen(genreView);
//        applyDim();
//        return genreView;
    }

    private void placeGenresOnScreen(View genreView) {
        genrePopup = new PopupWindow(context);
        genrePopup.setContentView(genreView);
        genrePopup.setFocusable(true);
        genrePopup.setWidth((int) (view.getWidth() * 0.3));
        genrePopup.showAtLocation(genreView, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0);
    }

    public void addViewsToLinearLayout(View gView, Genres genres, String currentGenre, int displayed) {
//        view.findViewById(R.id.all_genres).setVisibility(View.VISIBLE);
        LinearLayout linearLayout = gView.findViewById(R.id.genre_list);
        linearLayout.removeAllViews();
        linearLayout.addView(createGenreTextView(currentGenre, -1));
        for (int i = 0; i < genres.getSize(); i++) {
            String genre = genres.getGenres().get(i);
            if (!genre.equalsIgnoreCase(currentGenre)) {
                TextView genreTextView = createGenreTextView(genre, i);
                linearLayout.addView(genreTextView);
            }

        }
        if (!(displayed == MY_RECORDINGS))
            linearLayout.addView(addMyRecordingsOption());
        if (!(displayed == SONG_SUGGESTION))
            linearLayout.addView(addSongSuggestionOption());

    }

    private TextView createGenreTextView(String genre, int i) {
        TextView textView = new TextView(context);
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
        TextView textView = new TextView(context);
        textView.setTextColor(Color.WHITE);

        setTextViewAttributes(textView);
        String textToDisplay = context.getResources().getString(R.string.my_recordings);
        textView.setText(textToDisplay);

        textView.setOnClickListener(view -> {
            gListener.openMyRecordings();
            closeGenreChoices();

        });
        return textView;
    }

    private TextView addSongSuggestionOption() {
        TextView textView = new TextView(context);
        textView.setTextColor(context.getResources().getColor(R.color.pick_a_song_color));

        setTextViewAttributes(textView);
        String textToDisplay = context.getResources().getString(R.string.song_suggestion);
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
        ((TextView) view.findViewById(R.id.genre)).setText(genreToDisplay);
    }


    public interface GenreUIListener {

        void getAllSongsFromGenre(int i);

        void showSongSuggestionBox();

        void openMyRecordings();
    }
}