package com.example.demo.dto;

import com.example.demo.models.TrieNode;
import com.example.demo.trie.WordFreq;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class Node {
    public ObjectId id;
    public boolean dirty = false;
    public Node[] children = new Node[26];
    public boolean isTerminal = false;
    public int freq = 0;
    public String word;
    public List<WordFreq> topKFreq = new ArrayList<>();

    public Node(TrieNode node) {
        this.id = node.getId();
        this.isTerminal = node.isTerminal();
        this.freq = node.getFreq();
        this.word = node.getWord();
        this.dirty = false;
    }
}
