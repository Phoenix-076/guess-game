package com.example.countryguessgame.service;

import com.example.countryguessgame.model.Country;
import com.example.countryguessgame.model.GameQuestion;
import com.example.countryguessgame.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CountryService {

    @Autowired
    private CountryRepository countryRepository;
    private final Random random = new Random();

    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    public Optional<Country> getCountryById(Long id) {
        return countryRepository.findById(id);
    }

    public Country getRandomCountry() {
        List<Country> countries = countryRepository.findAll();
        if (countries.isEmpty()) {
            return null;
        }
        return countries.get(random.nextInt(countries.size()));
    }

    public boolean checkAnswer(String countryName, String guess) {
        return countryName.equalsIgnoreCase(guess.trim());
    }

    public Country saveCountry(Country country) {
        return countryRepository.save(country);
    }

    public Optional<Country> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return countryRepository.findByName(name.trim());
    }

    public GameQuestion getRandomQuestion(int optionCount, String difficulty) {
        List<Country> countries = countryRepository.findAll();
        List<Country> filtered = countries.stream()
                .filter(c -> difficulty == null || difficulty.isBlank() || c.getDifficulty() == null ||
                        c.getDifficulty().equalsIgnoreCase(difficulty))
                .toList();

        List<Country> source = filtered.isEmpty() ? countries : filtered;
        if (source.isEmpty()) {
            return null;
        }

        Country correctCountry = source.get(random.nextInt(source.size()));

        List<String> pool = source.stream()
                .map(Country::getName)
                .distinct()
                .collect(Collectors.toList());

        Collections.shuffle(pool, random);

        List<String> options = new ArrayList<>();
        options.add(correctCountry.getName());

        for (String candidate : pool) {
            if (options.size() >= optionCount) {
                break;
            }
            if (!candidate.equalsIgnoreCase(correctCountry.getName())) {
                options.add(candidate);
            }
        }

        Collections.shuffle(options, random);

        return new GameQuestion(correctCountry, options);
    }
}
