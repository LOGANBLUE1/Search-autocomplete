package com.example.demo.dto;


import com.example.demo.models.TrieNode;
import com.example.demo.trie.WordFreq;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class Node {
    public Node[] children = new Node[26];
    public boolean isTerminal = false;
    public int freq = 0; // frequency for completed words
    public String word;
    public List<WordFreq> topKFreq = new ArrayList<>();//5 most frequent words

    public Node(TrieNode node){
        this.isTerminal = node.isTerminal();
        this.freq = node.getFreq();
        this.word = node.getWord();
    }
}