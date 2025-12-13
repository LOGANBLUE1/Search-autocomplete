package com.example.demo.trie;

import com.example.demo.util.Utils;
import com.example.demo.dto.Node;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrieService implements TrieInterface {
    Utils utils = new Utils();
    @Getter
    public Node root;
    private final int MAX_WORD_LENGTH = 50;
    private final int ALPHABET_SIZE = 26;
    private int count = 0;

    public TrieService(Node root){
        this.root = root;
        this.populateTrie();
    }

    @Override
    public void insert(String word) {
        if(word.length() > MAX_WORD_LENGTH) {
            word = word.substring(0, MAX_WORD_LENGTH);
        }
        Node cur = root;
        for (char c : word.toCharArray()) {
            int i = c - 'a';
            if (cur.children[i] == null) {
                cur.children[i] = new Node();
                this.count++;
            }
            cur = cur.children[i];
        }
        cur.isTerminal = true;
        cur.freq++;
        cur.word = word;
    }
    @Override
    public boolean isPresent(String word) {
        Node n = nodeFor(word);
        return n != null && n.isTerminal;
    }
    @Override
    public boolean startsWith(String prefix) {
        return nodeFor(prefix) != null;
    }
    @Override
    public void populateTrie(){
        dfsCollect(root);
    }

    public void loadWords(String filepath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String w : words) {
                    String cleaned = w.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    if (!cleaned.isEmpty()) this.insert(cleaned);
                }
            }
        }
        this.populateTrie();
//        dbUtil.insertIntoDb(getRoot());
    }

    private Node nodeFor(String s) {
        Node cur = root;
        for (char c : s.toCharArray()) {
            int i = c - 'a';
            if (i < 0 || i >= ALPHABET_SIZE || cur.children[i] == null)
                    return null;
            cur = cur.children[i];
        }
        return cur;
    }

    public List<WordFreq> suggest(String prefix) {
        Node node = nodeFor(prefix);
        if (node == null)
                return Collections.emptyList();
        return node.topKFreq;
    }

    private ArrayList<WordFreq>  dfsCollect(Node node) {
        ArrayList <WordFreq>  listLocal = new ArrayList<>();
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (node.children[i] != null) {
                var list2 = dfsCollect(node.children[i]);
                utils.mergeTopK(listLocal, list2);
            }
        }
        if (node.isTerminal) {
            utils.addSafelyWithLimit(listLocal, new WordFreq(node.word, node.freq));
        }
        return (ArrayList<WordFreq>) (node.topKFreq = listLocal);
    }
}