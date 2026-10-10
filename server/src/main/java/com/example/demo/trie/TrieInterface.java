package com.example.demo.trie;

public interface TrieInterface {
    void insert(String word);

    void insert(String word, int count);

    boolean isPresent(String word);

    boolean startsWith(String prefix);

    void populateTrie();
}
