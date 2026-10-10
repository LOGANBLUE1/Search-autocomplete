package com.example.demo.scheduler;

import com.example.demo.service.TrieNodeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TriePopulateJob {

    private final TrieNodeService trieNodeService;

    public TriePopulateJob(TrieNodeService trieNodeService) {
        this.trieNodeService = trieNodeService;
    }

    /**
     * Every Monday at 2 AM: apply searches received since the last build
     * into the in-memory trie (incremental top-K), persist dirty nodes, evict cache.
     *
     * Cron: 0 0 2 ? * MON
     */
    @Scheduled(cron = "0 0 2 ? * MON")
    public void populateTrieWeekly() {
        System.out.println("Weekly trie delta apply starting...");
        int applied = trieNodeService.applyWeeklyDelta();
        System.out.println("Weekly trie delta apply finished. Words applied: " + applied);
    }
}
