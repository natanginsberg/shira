package com.function.karaoke.hardware.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOverlay;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.Genres;
import com.function.karaoke.hardware.utils.static_classes.Converter;

public class SongsActivityUI {

    private final View view;
    private final Context context;
    private final GenresUI genreUI;

    public SongsActivityUI(View songsActivity, GenresUI.GenreUIListener listener, String currentLanguage, Context context) {
        this.view = songsActivity;
        this.context = context;
        genreUI = new GenresUI(songsActivity, context, currentLanguage, listener);
    }

    private void applyDim() {
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, view.getWidth(), view.getHeight());
        dim.setAlpha((int) (255 * (float) 0.5));
        ViewOverlay overlay = view.getOverlay();
        overlay.add(dim);
    }

    public void addGenresToScreen(Genres genres, String currentGenre) {
        genreUI.addGenresToScreen(genres, currentGenre, -1);
//        view.findViewById(R.id.all_genres).setVisibility(View.VISIBLE);
//        LinearLayout linearLayout = view.findViewById(R.id.genre_list);
//        linearLayout.removeAllViews();
//        linearLayout.addView(createGenreTextView(currentGenre, -1));
//        for (int i = 0; i < genres.getSize(); i++) {
//            String genre = genres.getGenres().get(i);
//            if (!genre.equalsIgnoreCase(currentGenre)) {
//                TextView genreTextView = createGenreTextView(genre, i);
//                linearLayout.addView(genreTextView);
//            }
//
//        }
//        linearLayout.addView(addMyRecordingsOption());
//        linearLayout.addView(addSongSuggestionOption());
    }

    public View openSongSuggestionsPopup() {
        RelativeLayout viewGroup = view.findViewById(R.id.song_suggestion);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View suggestionView = layoutInflater.inflate(R.layout.song_suggestion, viewGroup);

        placeSuggestionOnScreen(suggestionView);
        applyDim();
        return suggestionView;
    }

    private void placeSuggestionOnScreen(View suggestionView) {
        PopupWindow suggestPopup = new PopupWindow(context);
        suggestPopup.setContentView(suggestionView);
        suggestPopup.setFocusable(true);
        suggestPopup.setHeight(Converter.convertDpToPx(250));
        suggestPopup.setWidth(Converter.convertDpToPx(250));
        suggestPopup.showAtLocation(suggestionView, Gravity.CENTER, 0, 0);

    }

    public void addGenreToScreen(String genre) {
        genreUI.addGenreToScreen(genre);
//        String genreToDisplay;
//        if (currentLanguage.equals("en")) {
//            genreToDisplay = genre.split(",")[0];
//        } else {
//            genreToDisplay = genre.split(",")[1];
//        }
//        ((TextView) view.findViewById(R.id.genre)).setText(genreToDisplay);
    }
}
