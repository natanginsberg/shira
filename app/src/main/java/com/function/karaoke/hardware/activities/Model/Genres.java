package com.function.karaoke.hardware.activities.Model;

import java.util.ArrayList;
import java.util.List;

public class Genres {

    private List<String> englishGenres = new ArrayList<>();
    private List<String> hebrewGenres = new ArrayList<>();

    public Genres() {
    }

    public Genres(List<String> englishGenres, List<String> hebrewGenres) {
        this.englishGenres = englishGenres;
        this.hebrewGenres = hebrewGenres;
    }

    public List<String> getEnglishGenres() {
        return englishGenres;
    }

    public void setEnglishGenres(List<String> englishGenres) {
        this.englishGenres = englishGenres;
    }

    public List<String> getHebrewGenres() {
        return hebrewGenres;
    }

    public void setHebrewGenres(List<String> hebrewGenres) {
        this.hebrewGenres = hebrewGenres;
    }
}
