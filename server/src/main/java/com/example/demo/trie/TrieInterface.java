package com.example.demo.trie;

public interface TrieInterface {
    void insert(String word);
    boolean isPresent(String word);
    boolean startsWith(String prefix);
    void populateTrie();
}
