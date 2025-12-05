package com.example.countryguessgame.controller;

import com.example.countryguessgame.model.Country;
import com.example.countryguessgame.model.GameQuestion;
import com.example.countryguessgame.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:4200")
public class GameController {

    @Autowired
    private CountryService countryService;

    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }

    @GetMapping("/random")
    public ResponseEntity<Country> getRandomCountry() {
        Country country = countryService.getRandomCountry();
        if (country != null) {
            return ResponseEntity.ok(country);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/question")
    public ResponseEntity<GameQuestion> getRandomQuestion(@RequestParam(defaultValue = "4") int options,
                                                          @RequestParam(defaultValue = "easy") String level) {
        GameQuestion question = countryService.getRandomQuestion(Math.max(2, options), level);
        if (question != null) {
            return ResponseEntity.ok(question);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAnswer(@RequestBody Map<String, String> request) {
        String countryName = request.get("countryName");
        String guess = request.get("guess");

        Map<String, Object> response = new HashMap<>();
        boolean isCorrect = countryService.checkAnswer(countryName, guess);

        response.put("correct", isCorrect);
        response.put("message", isCorrect ? "Correct! Well done!" : "Wrong answer. Try again!");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/countries")
    public ResponseEntity<Country> addCountry(@RequestBody Country country) {
        Country savedCountry = countryService.saveCountry(country);
        return ResponseEntity.ok(savedCountry);
    }
}
