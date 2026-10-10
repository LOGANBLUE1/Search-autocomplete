package com.example.demo.service;

import com.example.demo.models.Word;
import com.example.demo.repository.WordRepository;
import com.example.demo.util.WordNormalizer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WordService {
    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    /**
     * Record a completed user search: increment freq and bump updatedAt.
     * Does not update the in-memory trie (weekly job applies the delta).
     */
    public Word recordSearch(String raw) {
        String normalized = WordNormalizer.normalize(raw);
        if (normalized.isEmpty()) {
            return null;
        }

        Word word = wordRepository.findByWord(normalized).orElseGet(() -> new Word(normalized));
        word.setFreq(word.getFreq() + 1);
        word.setUpdatedAt(Instant.now());
        return wordRepository.save(word);
    }

    /** @deprecated Prefer {@link #recordSearch(String)}; kept for /store compatibility. */
    public Word insert(String word) {
        return recordSearch(word);
    }

    public List<Word> findUpdatedAfter(Instant since) {
        return wordRepository.findByUpdatedAtAfter(since);
    }

    public List<Word> findPendingDelta() {
        return wordRepository.findWithPendingDelta();
    }

    public void markApplied(Word word) {
        word.setAppliedFreq(word.getFreq());
        wordRepository.save(word);
    }
}
