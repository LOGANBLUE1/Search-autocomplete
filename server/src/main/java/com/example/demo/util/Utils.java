package com.example.demo.util;


import com.example.demo.trie.WordFreq;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    private final int MAX_SUGGESTION_LIMIT = 5;
    public void addSafelyWithLimit(ArrayList<WordFreq> list, WordFreq wordFreq) {
        for(int i = 0; i < list.size(); i++) {
            if (wordFreq.getFreq() > list.get(i).getFreq()) {
                list.add(i, wordFreq);
                if (list.size() > MAX_SUGGESTION_LIMIT) {
                    list.remove(list.getLast());
                }
                return;
            }
        }
        list.add(wordFreq);
        if (list.size() > MAX_SUGGESTION_LIMIT) {
            list.remove(list.getLast());
        }
    }
    public void mergeTopK(ArrayList<WordFreq> a, List<WordFreq> b) {
        int j = 0;
        for (int i = 0; i < Math.min(a.size(), MAX_SUGGESTION_LIMIT) && j < b.size(); i++) {
            if (b.get(j).getFreq()> a.get(i).getFreq()) {
                a.add(i, b.get(j++));
            }
        }
        while(a.size() < MAX_SUGGESTION_LIMIT && j < b.size()) {
            a.add(b.get(j++));
        }
        while(a.size() > MAX_SUGGESTION_LIMIT) {
            a.remove(a.getLast());
        }
    }
}
