package com.example.demo.repository;

import com.example.demo.models.TrieBuildState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrieBuildStateRepository extends MongoRepository<TrieBuildState, String> {
}
