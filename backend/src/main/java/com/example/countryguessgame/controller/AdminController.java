package com.example.countryguessgame.controller;

import com.example.countryguessgame.exception.InvalidCredentialsException;
import com.example.countryguessgame.model.Country;
import com.example.countryguessgame.model.dto.AdminAuthRequest;
import com.example.countryguessgame.model.dto.AdminCountryRequest;
import com.example.countryguessgame.service.CountryService;
import com.example.countryguessgame.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private CountryService countryService;

    @PostMapping("/leaderboard")
    public ResponseEntity<?> fullLeaderboard(@RequestBody AdminAuthRequest request) {
        try {
            scoreService.requireAdmin(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(scoreService.fullLeaderboard());
        } catch (InvalidCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Admin access required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/countries")
    public ResponseEntity<?> addCountry(@RequestBody AdminCountryRequest request) {
        try {
            scoreService.requireAdmin(request.getUsername(), request.getPassword());
        } catch (InvalidCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Admin access required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Country country = request.getCountry();
        Map<String, String> error = new HashMap<>();
        if (country == null || isBlank(country.getName()) || isBlank(country.getEmoji())) {
            error.put("error", "Country name and emoji are required");
            return ResponseEntity.badRequest().body(error);
        }
        if (isBlank(country.getDifficulty())) {
            country.setDifficulty("easy");
        }

        country.setName(country.getName().trim());
        country.setEmoji(country.getEmoji().trim());
        country.setDifficulty(country.getDifficulty().trim().toLowerCase());
        if (country.getHints() != null) {
            country.setHints(country.getHints().trim());
        }

        Optional<Country> existing = countryService.findByName(country.getName());
        if (existing.isPresent()) {
            error.put("error", "Country already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        Country saved = countryService.saveCountry(country);
        return ResponseEntity.ok(saved);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
