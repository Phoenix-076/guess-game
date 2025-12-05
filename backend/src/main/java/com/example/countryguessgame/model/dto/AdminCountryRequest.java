package com.example.countryguessgame.model.dto;

import com.example.countryguessgame.model.Country;

public class AdminCountryRequest extends AdminAuthRequest {
    private Country country;

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
