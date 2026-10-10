package com.example.demo.controller;

import com.example.demo.service.TrieNodeService;
import com.example.demo.service.WordService;
import com.example.demo.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/query")
public class AutoCompleteController {
    @Autowired
    private TrieNodeService service;

    @Autowired
    private WordService wordService;

    @GetMapping("")
    public ResponseEntity<?> getSuggestions(@RequestParam String query) {
        return ApiResponse.success(service.suggest(query));
    }

    /**
     * Record a completed search (Enter / suggestion click).
     * Does not update the live trie; the Monday job applies the delta.
     */
    @PostMapping("/record")
    public ResponseEntity<?> recordSearch(@RequestParam String query) {
        return ApiResponse.created(wordService.recordSearch(query));
    }
}
