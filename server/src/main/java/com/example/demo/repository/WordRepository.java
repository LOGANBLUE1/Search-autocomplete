package com.example.demo.repository;

import com.example.demo.models.Word;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends MongoRepository<Word, String> {
    Optional<Word> findByWord(String word);

    boolean existsByWord(String word);

    List<Word> findByUpdatedAtAfter(Instant updatedAt);

    /** Words whose recorded freq has not yet been applied to the trie. */
    @Query("{ $expr: { $gt: ['$freq', '$appliedFreq'] } }")
    List<Word> findWithPendingDelta();
}
