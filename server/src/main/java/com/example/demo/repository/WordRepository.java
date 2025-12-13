package com.example.demo.repository;

import com.example.demo.models.Word;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends MongoRepository<Word, String> {
    boolean existsByWord(String word);
}
