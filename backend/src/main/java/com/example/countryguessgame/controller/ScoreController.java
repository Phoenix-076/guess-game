package com.example.countryguessgame.controller;

import com.example.countryguessgame.model.Score;
import com.example.countryguessgame.exception.UsernameAlreadyExistsException;
import com.example.countryguessgame.exception.InvalidCredentialsException;
import com.example.countryguessgame.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        try {
            Score created = scoreService.register(username, password);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (UsernameAlreadyExistsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Username already taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        try {
            Score user = scoreService.login(username, password);
            return ResponseEntity.ok(user);
        } catch (InvalidCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitScore(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String bestScoreRaw = request.getOrDefault("bestScore", "0");

        if (username == null || username.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Username is required");
            return ResponseEntity.badRequest().body(error);
        }

        int bestScore;
        try {
            bestScore = Integer.parseInt(bestScoreRaw);
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "bestScore must be a number");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            Score updated = scoreService.updateBestScore(username, bestScore);
            return ResponseEntity.ok(updated);
        } catch (InvalidCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid user");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Score>> leaderboard() {
        return ResponseEntity.ok(scoreService.leaderboard());
    }
}
