package com.function.karaoke.hardware.activities.Model;

import java.util.ArrayList;
import java.util.List;

/***
 * a class that although funcs are not used they are needed for the firestore
 */
public class Genres {

    private List<String> genres = new ArrayList<>();

    public Genres() {
    }

    public Genres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getGenres() {
        return genres;
    }

}
