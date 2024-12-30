package org.example.response;

import java.io.Serializable;

public class CountryRank implements Serializable {
    String country;
    int score;

    public CountryRank(String country, int score) {
        this.country = country;
        this.score = score;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
