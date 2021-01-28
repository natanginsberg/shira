package com.function.karaoke.hardware.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOverlay;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.utils.static_classes.Converter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class SongsActivityUI {

    private static final int ALL_SONGS_DISPLAYED = 1;
    private static final int PERSONAL_RECORDING_DISPLAYED = 2;

    private final View view;
    private final SongsUIListener listener;
    private final String currentLanguage;
    private View popupView;
    private PopupWindow popup;
    private final Context context;
    private TextView allSongsTextView;
    private TextView genreClicked;
    private ImageView profilePic;

    public SongsActivityUI(View songsActivity, SongsUIListener listener, String currentLanguage, Context context) {
        this.view = songsActivity;
        this.listener = listener;
        this.context = context;
        this.currentLanguage = currentLanguage;
    }

    public void openSettingsPopup(boolean isUserSignedIn, int contentDisplayed) {
        RelativeLayout viewGroup = view.findViewById(R.id.settings_popup);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.settings_popup, viewGroup);
        profilePic = popupView.findViewById(R.id.user_picture);
        if (contentDisplayed == PERSONAL_RECORDING_DISPLAYED) {
//        if (((TextView) view.findViewById(R.id.display_text)).getText() == context.getResources().getString(R.string.my_recordings)) {
            ((TextView) popupView.findViewById(R.id.my_recordings)).setTextColor(context.getResources().getColor(R.color.gold));
        } else
//            ((TextView) popupView.findViewById(R.id.home_button)).setTextColor(context.getResources().getColor(R.color.gold));
            setSignInOrOut(isUserSignedIn);

        placePopupOnScreen();
        applyDim();

    }

    private void setSignInOrOut(boolean isSignedIn) {
        TextView signInOrOutButton = ((TextView) popupView.findViewById(R.id.sign_in_button));
        if (isSignedIn)
            signInOrOutButton.setText(context.getResources().getText(R.string.sign_out));
        else
            signInOrOutButton.setText(context.getResources().getText(R.string.sign_in));
    }

    public void setEmailAddressIfSignedIn(String emailAddressIfSignedIn) {
        ((TextView) popupView.findViewById(R.id.email_address)).setText(emailAddressIfSignedIn);
    }

    private void placePopupOnScreen() {
        popup = new PopupWindow(context);
        popup.setFocusable(true);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                view.getOverlay().clear();

            }
        });
        setPopupAttributes(popup, popupView);
//        int[] location = new int[2];
//        view.findViewById(R.id.settings_button).getLocationOnScreen(location);
        popup.showAtLocation(popupView, Gravity.TOP | Gravity.CENTER, 0, 0);
//                Math.abs(view.getWidth() - location[0]) < Math.abs(location[0]) ? view.getWidth() : 0, 0);
    }

    private void applyDim() {
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, view.getWidth(), view.getHeight());
        dim.setAlpha((int) (255 * (float) 0.5));
        ViewOverlay overlay = view.getOverlay();
//        ViewOverlay headerOverlay = headerView.getOverlay();
//        headerOverlay.add(dim);
        overlay.add(dim);
    }

    private void setPopupAttributes(PopupWindow popup, View layout) {
        int width = (int) (view.getWidth());
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight((int) (view.getHeight() * 0.8));
    }

    public PopupWindow getPopup() {
        return popup;
    }

    public View getPopupView() {
        return popupView;
    }

    public void changeTextForSignInButton(CharSequence text) {
        ((TextView) popupView.findViewById(R.id.sign_in_button)).setText(text);
    }

//    public void openSearchBar(boolean isSearchOpen) {
//        if (isSearchOpen)
//            view.findViewById(R.id.search_input).setVisibility(View.GONE);
//
//        else
//            view.findViewById(R.id.search_input).setVisibility(View.VISIBLE);
//
//    }
//
//    public void addGenresToScreen(Genres genres) {
//        LinearLayout linearLayout = view.findViewById(R.id.genres);
//        List<String> currentLanguageGenres;
//
//        currentLanguageGenres = genres.getGenres();
//
//        for (int i = 0; i < currentLanguageGenres.size(); i++) {
//            TextView genre = setGenreBar(currentLanguageGenres, i);
//
//            linearLayout.addView(genre);
//            genre.post(new Runnable() {
//                @Override
//                public void run() {
//                    HorizontalScrollView hz = view.findViewById(R.id.genre_scrolling);
//                    linearLayout.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (currentLanguage.equals("iw"))
//                                hz.fullScroll(View.FOCUS_RIGHT);
//                        }
//                    });
//                }
//            });
//        }
//        HorizontalScrollView hz = view.findViewById(R.id.genre_scrolling);
//        linearLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                if (currentLanguage.equals("iw"))
//                    hz.fullScroll(View.FOCUS_RIGHT);
//            }
//        });
//    }
//
//    public void colorNextGenre(int genre) {
//        TextView newGenre = (TextView) view.findViewById(genre);
//        setTextOfClickedToBlack();
//        setGenreClicked(newGenre);
//    }

//    private TextView setGenreBar(List<String> currentLanguageGenres, int i) {
//        String genre = currentLanguageGenres.get(i);
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        TextView textView = (TextView) inflater.inflate(R.layout.genre_layout, null);
//        textView.setTextColor(Color.BLACK);
//        textView.setId(i);
//
//        setTextViewAttributes(textView);
//        String genreToDisplay;
//        if (currentLanguage.equals("en")) {
//            genreToDisplay = genre.split(",")[0];
//        } else {
//            genreToDisplay = genre.split(",")[1];
//        }
//        String textToDisplay = "   " + genreToDisplay + "   |";
//        textView.setText(textToDisplay);
////        textView.setTypeface(tf);
//        if (genre.contains("כל השירים")) {
//            setGenreClicked(textView);
//            allSongsTextView = textView;
//        }
//        int finalI = i;
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (genreClicked != textView) {
//                    setTextOfClickedToBlack();
//                    setGenreClicked(textView);
//                    listener.getAllSongsFromGenre(i);
//                } else {
//                    listener.getAllSongsFromGenre(0);
//                    setTextOfClickedToBlack();
//                    setGenreClicked(allSongsTextView);
//                }
//
//            }
//        });
//        return textView;
//    }

//    private void setTextViewAttributes(TextView textView) {
//        textView.setHeight(Converter.convertDpToPx(24));
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
//        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//        Typeface tf = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/varela_round_regular.ttf");
//        textView.setTypeface(tf);
//    }
//
//    private void setTextOfClickedToBlack() {
//        genreClicked.setTextColor(Color.BLACK);
//    }
//
//    private void setGenreClicked(TextView textView) {
//        textView.setTextColor(context.getResources().getColor(R.color.gold));
//        genreClicked = textView;
//    }

    public void noRecordings() {
        view.findViewById(R.id.no_recordings_text).setVisibility(View.VISIBLE);
        view.findViewById(R.id.list).setVisibility(View.INVISIBLE);
    }

//    public void hideGenresAndSearch() {
//        view.findViewById(R.id.genre_scrolling).setVisibility(View.INVISIBLE);
////        view.findViewById(R.id.open_search).setVisibility(View.INVISIBLE);
//    }
//
//    public void showGenresAndSearch() {
//        view.findViewById(R.id.genre_scrolling).setVisibility(View.VISIBLE);
////        view.findViewById(R.id.open_search).setVisibility(View.VISIBLE);
//    }
//
//    public void allSongsShow() {
//        view.findViewById(R.id.list).setVisibility(View.VISIBLE);
////        ((TextView) popupView.findViewById(R.id.home_button)).setTextColor(context.getResources().getColor(R.color.gold));
//        ((TextView) popupView.findViewById(R.id.my_recordings)).setTextColor(context.getResources().getColor(R.color.sing_up_hover));
////        ((TextView) view.findViewById(R.id.display_text)).setText(R.string.all_songs);
//        view.findViewById(R.id.no_recordings_text).setVisibility(View.INVISIBLE);
//    }

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
        String genreToDisplay;
        if (currentLanguage.equals("en")) {
            genreToDisplay = genre.split(",")[0];
        } else {
            genreToDisplay = genre.split(",")[1];
        }
        ((TextView) view.findViewById(R.id.genre)).setText(genreToDisplay);
    }

    public void addPicToScreen(UserInfo userInfo) {
        Picasso.get()
                .load(userInfo.getPicUrl())
                .placeholder(R.drawable.circle)
                .fit()
                .transform(new CropCircleTransformation())
                .into(profilePic);


    }

    public interface SongsUIListener {
        void getAllSongsFromGenre(int genre);
    }
}
