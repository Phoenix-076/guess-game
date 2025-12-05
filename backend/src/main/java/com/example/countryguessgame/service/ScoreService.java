package com.example.countryguessgame.service;

import com.example.countryguessgame.exception.InvalidCredentialsException;
import com.example.countryguessgame.exception.UsernameAlreadyExistsException;
import com.example.countryguessgame.model.Score;
import com.example.countryguessgame.repository.ScoreRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Value("${app.admin.username:admin}")
    private String defaultAdminUsername;

    @Value("${app.admin.password:admin123}")
    private String defaultAdminPassword;

    @PostConstruct
    public void ensureDefaultAdminExists() {
        String adminUser = normalize(defaultAdminUsername);
        String adminPass = normalize(defaultAdminPassword);
        if (adminUser.isBlank() || adminPass.isBlank()) {
            return;
        }
        Optional<Score> existing = scoreRepository.findByUsernameIgnoreCase(adminUser);
        if (existing.isEmpty()) {
            Score admin = new Score(adminUser, adminPass, 0);
            admin.setRole("ADMIN");
            scoreRepository.save(admin);
            return;
        }
        Score admin = existing.get();
        admin.setRole("ADMIN");
        admin.setPassword(adminPass);
        scoreRepository.save(admin);
    }

    public Score register(String usernameRaw, String passwordRaw) {
        String username = normalize(usernameRaw);
        String password = normalize(passwordRaw);
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required");
        }
        Optional<Score> existing = scoreRepository.findByUsernameIgnoreCase(username);
        if (existing.isPresent()) {
            throw new UsernameAlreadyExistsException(username);
        }
        Score score = new Score(username, password, 0);
        score.setRole("PLAYER");
        return scoreRepository.save(score);
    }

    public Score login(String usernameRaw, String passwordRaw) {
        String username = normalize(usernameRaw);
        String password = normalize(passwordRaw);
        Optional<Score> existing = scoreRepository.findByUsernameIgnoreCase(username);
        if (existing.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        Score user = existing.get();
        if (!user.getPassword().equals(password)) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    public Score requireAdmin(String usernameRaw, String passwordRaw) {
        Score user = login(usernameRaw, passwordRaw);
        if (!user.isAdmin()) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    @Transactional
    public Score addPoints(String usernameRaw, int points) {
        String username = normalize(usernameRaw);
        Optional<Score> existing = scoreRepository.findByUsernameIgnoreCase(username);
        if (existing.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        Score score = existing.get();
        if (score.isAdmin()) {
            throw new InvalidCredentialsException();
        }
        score.setTotalScore(score.getTotalScore() + points);
        return scoreRepository.save(score);
    }

    public List<Score> leaderboard() {
        return scoreRepository.findTop10ByRoleNotOrderByTotalScoreDescIdAsc("ADMIN");
    }

    public List<Score> fullLeaderboard() {
        return scoreRepository.findAllByRoleNotOrderByTotalScoreDescIdAsc("ADMIN");
    }

    private String normalize(String usernameRaw) {
        return usernameRaw == null ? "" : usernameRaw.trim();
    }

    @Transactional
    public Score updateBestScore(String usernameRaw, int bestScore) {
        String username = normalize(usernameRaw);
        Optional<Score> existing = scoreRepository.findByUsernameIgnoreCase(username);
        if (existing.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        Score score = existing.get();
        if (score.isAdmin()) {
            throw new InvalidCredentialsException();
        }
        score.setTotalScore(Math.max(score.getTotalScore(), Math.max(bestScore, 0)));
        return scoreRepository.save(score);
    }
}
