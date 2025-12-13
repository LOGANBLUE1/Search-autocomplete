package com.example.demo.controller;

import com.example.demo.service.WordService;
import com.example.demo.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
public class WordStoreController {
    @Autowired
    private WordService service;
    @PostMapping("")
    public ResponseEntity<?> storeWord(@RequestParam String word) {
        return ApiResponse.created(service.insert(word));
    }
}
