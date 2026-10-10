package com.example.demo.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "trieBuildState")
@NoArgsConstructor
public class TrieBuildState {
    public static final String SINGLETON_ID = "singleton";

    @Id
    private String id = SINGLETON_ID;
    private Instant lastBuildAt;
    private String rootNodeId;

    public TrieBuildState(Instant lastBuildAt, String rootNodeId) {
        this.id = SINGLETON_ID;
        this.lastBuildAt = lastBuildAt;
        this.rootNodeId = rootNodeId;
    }
}
