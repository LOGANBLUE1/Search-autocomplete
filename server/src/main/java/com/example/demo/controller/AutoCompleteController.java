package com.example.demo.controller;

import com.example.demo.service.TrieNodeService;
import com.example.demo.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/query")
public class AutoCompleteController {
    @Autowired
    private TrieNodeService service;


    @GetMapping("")
    public ResponseEntity<?> getSuggestions(@RequestParam String query) {
        return ApiResponse.success(service.suggest(query));
    }
}
