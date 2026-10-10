package com.example.demo.service;

import com.example.demo.dto.Node;
import com.example.demo.models.TrieBuildState;
import com.example.demo.models.TrieNode;
import com.example.demo.models.Word;
import com.example.demo.repository.TrieBuildStateRepository;
import com.example.demo.repository.TrieNodeRepository;
import com.example.demo.trie.TrieService;
import com.example.demo.trie.WordFreq;
import jakarta.annotation.PostConstruct;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrieNodeService {
    private int count = 0;
    private final TrieNodeRepository repo;
    private final TrieBuildStateRepository buildStateRepository;
    private final WordService wordService;
    private TrieService trieService;

    @Value("${mongodb.root-node}")
    private String rootNodeId;

    public TrieNodeService(
            TrieNodeRepository repo,
            TrieBuildStateRepository buildStateRepository,
            WordService wordService) {
        this.repo = repo;
        this.buildStateRepository = buildStateRepository;
        this.wordService = wordService;
    }

    public TrieNode findById(ObjectId id) {
        return repo.findById(id).orElse(null);
    }

    public TrieNode insert(TrieNode node) {
        return repo.save(node);
    }

    public TrieService getTrieService() {
        return trieService;
    }

    /**
     * Bulk-load all trie nodes, then wire children in memory (no per-node round trips).
     */
    private Node loadTrieBulk(ObjectId rootId) {
        List<TrieNode> all = repo.findAll();
        Map<ObjectId, TrieNode> byId = new HashMap<>(all.size() * 2);
        for (TrieNode tn : all) {
            byId.put(tn.getId(), tn);
        }
        this.count = 0;
        return buildNodeFromMap(rootId, byId);
    }

    private Node buildNodeFromMap(ObjectId id, Map<ObjectId, TrieNode> byId) {
        if (id == null) {
            return null;
        }
        TrieNode trieNode = byId.get(id);
        if (trieNode == null) {
            return null;
        }
        this.count++;
        Node node = new Node(trieNode);

        List<WordFreq> topDocs = trieNode.getTopKFreq();
        if (topDocs != null) {
            node.topKFreq = new ArrayList<>(topDocs.size());
            for (WordFreq td : topDocs) {
                node.topKFreq.add(new WordFreq(td.getWord(), td.getFreq()));
            }
        } else {
            node.topKFreq = new ArrayList<>();
        }

        List<ObjectId> childIds = trieNode.getChildren();
        if (childIds != null) {
            for (int i = 0; i < childIds.size() && i < 26; i++) {
                ObjectId childId = childIds.get(i);
                if (childId != null) {
                    node.children[i] = buildNodeFromMap(childId, byId);
                }
            }
        }
        return node;
    }

    /**
     * Persist only dirty nodes (and newly created ones). Bottom-up so child ObjectIds exist.
     */
    public ObjectId persistDirtyNodes(Node node) {
        if (node == null) {
            return null;
        }

        List<ObjectId> childrenIds = new ArrayList<>(Collections.nCopies(26, null));
        for (int i = 0; i < 26; i++) {
            Node child = node.children[i];
            if (child != null) {
                childrenIds.set(i, persistDirtyNodes(child));
            }
        }

        if (node.dirty || node.id == null) {
            TrieNode trieNode = new TrieNode();
            if (node.id != null) {
                trieNode.setId(node.id);
            }
            trieNode.setTerminal(node.isTerminal);
            trieNode.setFreq(node.freq);
            trieNode.setWord(node.word);
            trieNode.setTopKFreq(node.topKFreq);
            trieNode.setChildren(childrenIds);

            TrieNode saved = this.insert(trieNode);
            node.id = saved.getId();
            node.dirty = false;
        }
        return node.id;
    }

    /** Full rewrite helper (rare); prefer {@link #persistDirtyNodes(Node)}. */
    public ObjectId insertNode(Node node) {
        if (node == null) {
            return null;
        }

        List<ObjectId> childrenIds = new ArrayList<>(26);
        for (int i = 0; i < 26; i++) {
            Node child = node.children[i];
            if (child == null) {
                childrenIds.add(null);
            } else {
                childrenIds.add(insertNode(child));
            }
        }

        TrieNode trieNode = new TrieNode();
        trieNode.setTerminal(node.isTerminal);
        trieNode.setFreq(node.freq);
        trieNode.setWord(node.word);
        trieNode.setTopKFreq(node.topKFreq);
        trieNode.setChildren(childrenIds);

        return this.insert(trieNode).getId();
    }

    @Cacheable(value = "searchCache", key = "#prefix")
    public List<String> suggest(String prefix) {
        if (trieService == null) {
            return List.of();
        }
        return trieService.suggest(prefix).stream()
                .map(WordFreq::getWord)
                .toList();
    }

    @CacheEvict(value = "searchCache", allEntries = true)
    public void evictSearchCache() {
        // annotation-driven eviction
    }

    /**
     * Apply words updated since lastBuildAt into the in-memory trie with incremental top-K,
     * persist dirty nodes, update build state, and evict Redis cache.
     */
    @CacheEvict(value = "searchCache", allEntries = true)
    public synchronized int applyWeeklyDelta() {
        if (trieService == null) {
            return 0;
        }

        TrieBuildState state = buildStateRepository
                .findById(TrieBuildState.SINGLETON_ID)
                .orElseGet(() -> new TrieBuildState(Instant.EPOCH, rootNodeId));

        List<Word> pending = wordService.findPendingDelta();

        int applied = 0;
        for (Word word : pending) {
            int delta = word.pendingDelta();
            if (delta <= 0) {
                continue;
            }
            trieService.insert(word.getWord(), delta);
            wordService.markApplied(word);
            applied++;
        }

        Node root = trieService.getRoot();
        ObjectId newRootId = persistDirtyNodes(root);
        if (newRootId != null) {
            this.rootNodeId = newRootId.toHexString();
        }

        Instant now = Instant.now();
        state.setLastBuildAt(now);
        state.setRootNodeId(this.rootNodeId);
        buildStateRepository.save(state);

        return applied;
    }

    @PostConstruct
    public void init() {
        resolveRootNodeId();
        System.out.println("Loading trie from DB (bulk)...");
        ObjectId rootId = new ObjectId(this.rootNodeId);
        Node root = loadTrieBulk(rootId);
        if (root == null) {
            System.out.println("Root node not found; starting with empty trie");
            root = new Node();
            root.dirty = true;
        }
        System.out.println("Total nodes loaded from DB: " + this.count);

        // Skip dfsCollect — topKFreq already persisted on each node
        trieService = new TrieService(root, false);
        System.out.println("Trie ready (no full top-K rebuild)");

        ensureBuildState();
    }

    private void resolveRootNodeId() {
        buildStateRepository.findById(TrieBuildState.SINGLETON_ID).ifPresent(state -> {
            if (state.getRootNodeId() != null && !state.getRootNodeId().isBlank()) {
                this.rootNodeId = state.getRootNodeId();
            }
        });
    }

    private void ensureBuildState() {
        if (!buildStateRepository.existsById(TrieBuildState.SINGLETON_ID)) {
            buildStateRepository.save(new TrieBuildState(Instant.EPOCH, rootNodeId));
        }
    }
}
