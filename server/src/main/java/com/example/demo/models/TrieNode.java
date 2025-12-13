package com.example.demo.models;

import com.example.demo.trie.WordFreq;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "trieNodes")
public class TrieNode {
    @Id
    private ObjectId id;

    private boolean isTerminal;
    private int freq;
    private String word;

    private List<WordFreq> topKFreq;
    private List<ObjectId> children;
}

