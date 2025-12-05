package com.example.countryguessgame.model;

import java.util.List;

public class GameQuestion {
    private Country country;
    private List<String> options;

    public GameQuestion(Country country, List<String> options) {
        this.country = country;
        this.options = options;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
