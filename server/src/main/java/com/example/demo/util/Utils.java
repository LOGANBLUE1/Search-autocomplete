package com.example.demo.util;

import com.example.demo.trie.WordFreq;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    private final int MAX_SUGGESTION_LIMIT = 5;

    public void addSafelyWithLimit(ArrayList<WordFreq> list, WordFreq wordFreq) {
        for (int i = 0; i < list.size(); i++) {
            if (wordFreq.getFreq() > list.get(i).getFreq()) {
                list.add(i, wordFreq);
                if (list.size() > MAX_SUGGESTION_LIMIT) {
                    list.remove(list.size() - 1);
                }
                return;
            }
        }
        list.add(wordFreq);
        if (list.size() > MAX_SUGGESTION_LIMIT) {
            list.remove(list.size() - 1);
        }
    }

    /**
     * Insert or update a word in a sorted top-K list (freq descending).
     */
    public void upsertTopK(List<WordFreq> list, String word, int freq) {
        list.removeIf(wf -> wf.getWord().equals(word));
        if (list.size() < MAX_SUGGESTION_LIMIT
                || freq > list.get(list.size() - 1).getFreq()) {
            ArrayList<WordFreq> asArray = list instanceof ArrayList
                    ? (ArrayList<WordFreq>) list
                    : new ArrayList<>(list);
            addSafelyWithLimit(asArray, new WordFreq(word, freq));
            if (list != asArray) {
                list.clear();
                list.addAll(asArray);
            }
        }
    }

    /**
     * Merge two sorted (freq desc) top-K lists into {@code a}, keeping top K.
     */
    public void mergeTopK(ArrayList<WordFreq> a, List<WordFreq> b) {
        if (b == null || b.isEmpty()) {
            return;
        }
        ArrayList<WordFreq> merged = new ArrayList<>(MAX_SUGGESTION_LIMIT);
        int i = 0;
        int j = 0;
        while (merged.size() < MAX_SUGGESTION_LIMIT && (i < a.size() || j < b.size())) {
            if (j >= b.size() || (i < a.size() && a.get(i).getFreq() >= b.get(j).getFreq())) {
                WordFreq candidate = a.get(i++);
                if (merged.stream().noneMatch(w -> w.getWord().equals(candidate.getWord()))) {
                    merged.add(candidate);
                }
            } else {
                WordFreq candidate = b.get(j++);
                if (merged.stream().noneMatch(w -> w.getWord().equals(candidate.getWord()))) {
                    merged.add(candidate);
                }
            }
        }
        a.clear();
        a.addAll(merged);
    }
}
