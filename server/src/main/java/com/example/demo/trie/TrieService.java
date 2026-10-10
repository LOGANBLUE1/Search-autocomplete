package com.example.demo.trie;

import com.example.demo.dto.Node;
import com.example.demo.util.Utils;
import com.example.demo.util.WordNormalizer;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrieService implements TrieInterface {
    private final Utils utils = new Utils();
    @Getter
    public Node root;
    private final int ALPHABET_SIZE = 26;
    private int count = 0;

    /**
     * @param rebuildTopK when true, run full dfsCollect (one-shot corpus import).
     *                    When loading from Mongo with precomputed topKFreq, pass false.
     */
    public TrieService(Node root, boolean rebuildTopK) {
        this.root = root != null ? root : new Node();
        if (rebuildTopK) {
            this.populateTrie();
        }
    }

    public TrieService(Node root) {
        this(root, false);
    }

    @Override
    public void insert(String word) {
        insert(word, 1);
    }

    @Override
    public void insert(String word, int count) {
        if (count <= 0) {
            return;
        }
        String normalized = WordNormalizer.normalize(word);
        if (normalized.isEmpty()) {
            return;
        }

        List<Node> path = new ArrayList<>(normalized.length() + 1);
        Node cur = root;
        path.add(cur);

        for (int idx = 0; idx < normalized.length(); idx++) {
            int i = normalized.charAt(idx) - 'a';
            if (i < 0 || i >= ALPHABET_SIZE) {
                return;
            }
            if (cur.children[i] == null) {
                cur.children[i] = new Node();
                cur.dirty = true;
                this.count++;
            }
            cur = cur.children[i];
            path.add(cur);
        }

        cur.isTerminal = true;
        cur.freq += count;
        cur.word = normalized;
        cur.dirty = true;

        for (Node node : path) {
            utils.upsertTopK(node.topKFreq, normalized, cur.freq);
            node.dirty = true;
        }
    }

    @Override
    public boolean isPresent(String word) {
        Node n = nodeFor(WordNormalizer.normalize(word));
        return n != null && n.isTerminal;
    }

    @Override
    public boolean startsWith(String prefix) {
        return nodeFor(WordNormalizer.normalize(prefix)) != null;
    }

    @Override
    public void populateTrie() {
        dfsCollect(root);
    }

    public void loadWords(String filepath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String w : words) {
                    String cleaned = WordNormalizer.normalize(w);
                    if (!cleaned.isEmpty()) {
                        // Path-only insert without top-K; full rebuild once at end
                        insertPathOnly(cleaned);
                    }
                }
            }
        }
        this.populateTrie();
    }

    /** Insert word path without updating ancestor top-K (used before full dfsCollect). */
    private void insertPathOnly(String word) {
        Node cur = root;
        for (int idx = 0; idx < word.length(); idx++) {
            int i = word.charAt(idx) - 'a';
            if (i < 0 || i >= ALPHABET_SIZE) {
                return;
            }
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

    private Node nodeFor(String s) {
        if (s == null || s.isEmpty()) {
            return root;
        }
        Node cur = root;
        for (int idx = 0; idx < s.length(); idx++) {
            int i = s.charAt(idx) - 'a';
            if (i < 0 || i >= ALPHABET_SIZE || cur.children[i] == null) {
                return null;
            }
            cur = cur.children[i];
        }
        return cur;
    }

    public List<WordFreq> suggest(String prefix) {
        String normalized = WordNormalizer.normalize(prefix);
        Node node = nodeFor(normalized);
        if (node == null) {
            return Collections.emptyList();
        }
        return node.topKFreq != null ? node.topKFreq : Collections.emptyList();
    }

    private ArrayList<WordFreq> dfsCollect(Node node) {
        ArrayList<WordFreq> listLocal = new ArrayList<>();
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (node.children[i] != null) {
                ArrayList<WordFreq> list2 = dfsCollect(node.children[i]);
                utils.mergeTopK(listLocal, list2);
            }
        }
        if (node.isTerminal) {
            utils.addSafelyWithLimit(listLocal, new WordFreq(node.word, node.freq));
        }
        node.topKFreq = listLocal;
        return listLocal;
    }
}
