package com.example.demo.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "words")
@NoArgsConstructor
public class Word {
    @Id
    private String id;
    private String word;

    public Word(String word) {
        this.word = word;
    }
}
