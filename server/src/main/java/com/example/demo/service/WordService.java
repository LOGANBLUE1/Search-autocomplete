package com.example.demo.service;

import com.example.demo.models.Word;
import com.example.demo.repository.WordRepository;
import org.springframework.stereotype.Service;

@Service
public class WordService {
    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public Word insert(String word) {
        if (word == null || word.isBlank()) return null;

        String normalized = word.trim().toLowerCase();

        if (!wordRepository.existsByWord(normalized)) {
            return wordRepository.save(new Word(normalized));
        }
        return null;
    }
}
