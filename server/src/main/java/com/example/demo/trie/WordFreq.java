package com.example.demo.trie;

import lombok.Data;

@Data
public class WordFreq {
    private final String word;
    private final int freq;
    public WordFreq(String word, int freq) {
        this.word = word;
        this.freq = freq;
    }
}
