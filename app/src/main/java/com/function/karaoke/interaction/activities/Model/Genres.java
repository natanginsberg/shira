package com.function.karaoke.interaction.activities.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/***
 * a class that although funcs are not used they are needed for the firestore
 */
public class Genres implements Serializable {

    private List<String> genres = new ArrayList<>();

    public Genres() {
    }

    public Genres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getGenres() {
        return genres;
    }

    public int getSize() {
        return genres.size();
    }

    public void add(Genres products) {
        for (String genre : genres) {
            if (!genres.contains(genre))
                genres.add(genre);
        }
    }
}
