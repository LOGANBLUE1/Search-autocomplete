package com.example.demo.scheduler;

import com.example.demo.trie.TrieInterface;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TriePopulateJob {

//    private final TrieInterface trie;
//
//    public TriePopulateJob(TrieInterface trie) {
//        this.trie = trie;
//    }

//
//        0 0 2 ? * MON
//    │ │ │ │ │ └─ Monday
//    │ │ │ │ └── every month
//    │ │ │ └──── day-of-month ignored
//    │ │ └────── 2 AM
//    │ └──────── 0 minutes
//    └────────── 0 seconds

    @Scheduled(cron = "0 0 2 ? * MON") // every Monday at 2 AM
    public void populateTrieWeekly() {

    }
}
