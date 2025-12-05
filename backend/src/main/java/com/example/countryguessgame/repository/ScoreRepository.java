package com.example.countryguessgame.repository;

import com.example.countryguessgame.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByUsernameIgnoreCase(String username);
    List<Score> findTop10ByOrderByTotalScoreDescIdAsc();
    List<Score> findAllByOrderByTotalScoreDescIdAsc();
    List<Score> findTop10ByRoleNotOrderByTotalScoreDescIdAsc(String role);
    List<Score> findAllByRoleNotOrderByTotalScoreDescIdAsc(String role);
}
