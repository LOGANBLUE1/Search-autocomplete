package com.example.demo.repository;

import com.example.demo.models.TrieNode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrieNodeRepository extends MongoRepository<TrieNode, ObjectId> {
}