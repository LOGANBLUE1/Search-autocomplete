package com.example.demo.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "words")
@NoArgsConstructor
public class Word {
    @Id
    private String id;

    @Indexed(unique = true)
    private String word;

    private int freq;
    /** Frequency already applied into the persisted trie. */
    private int appliedFreq;
    private Instant updatedAt;

    public Word(String word) {
        this.word = word;
        this.freq = 0;
        this.appliedFreq = 0;
        this.updatedAt = Instant.now();
    }

    public int pendingDelta() {
        return Math.max(0, freq - appliedFreq);
    }
}
