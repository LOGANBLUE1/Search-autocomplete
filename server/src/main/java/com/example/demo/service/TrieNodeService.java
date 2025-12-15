package com.example.demo.service;

import com.example.demo.models.TrieNode;
import com.example.demo.repository.TrieNodeRepository;
import com.example.demo.dto.Node;
import com.example.demo.trie.TrieService;
import com.example.demo.trie.WordFreq;
import jakarta.annotation.PostConstruct;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrieNodeService {
    private int count = 0;
    private final TrieNodeRepository repo;
    private TrieService trieService;

    @Value("${mongodb.root-node}")
    private String rootNodeId;

    public TrieNodeService(TrieNodeRepository repo) {
        this.repo = repo;
    }

    public TrieNode findById(ObjectId id) {
        return repo.findById(id).orElse(null);
    }

    public TrieNode insert(TrieNode node) {
        return repo.save(node);
    }

    public TrieNode deleteAll() {
        repo.deleteAll();
        return null;
    }

    private Node loadNodeFromDb(ObjectId id) {
        TrieNode trieNode = this.findById(id);
        if (trieNode == null) return null;
        this.count++;
        Node node = new Node(trieNode);

        var topDocs = trieNode.getTopKFreq();
        if (topDocs != null) {
            node.topKFreq = new ArrayList<>();
            for (var td : topDocs) {
                node.topKFreq.add(new WordFreq(td.getWord(), td.getFreq()));
            }
        }

        List<ObjectId> childIds = trieNode.getChildren();
        if (childIds != null) {
            for (int i = 0; i < childIds.size() && i < 26; i++) {
                ObjectId childId = childIds.get(i);
                if (childId != null) {
                    node.children[i] = loadNodeFromDb(childId);
                } else {
                    node.children[i] = null;
                }
            }
        }

        return node;
    }

    public ObjectId insertNode(Node node) {
        if (node == null) return null;

        List<ObjectId> childrenIds = new ArrayList<>(26);
        for (int i = 0; i < 26; i++) {
            Node child = node.children[i];
            if (child == null) {
                childrenIds.add(null);
            } else {
                ObjectId childId = insertNode(child);
                childrenIds.add(childId);
            }
        }

        TrieNode trieNode = new TrieNode();
        trieNode.setTerminal(node.isTerminal);
        trieNode.setFreq(node.freq);
        trieNode.setWord(node.word);
        trieNode.setTopKFreq(node.topKFreq);
        trieNode.setChildren(childrenIds);

        var res = this.insert(trieNode);
        return res.getId();
    }

    @Cacheable(value = "searchCache", key = "#prefix")
    public List<String> suggest(String prefix) {
        var listWithWordFreq = trieService.suggest(prefix);
        return listWithWordFreq.stream()
                .map(WordFreq::getWord)
                .toList();
    }

    @CacheEvict(value = "searchCache", allEntries = true)
    private Node loadTrieFromDb() {
        ObjectId rootId = new ObjectId(this.rootNodeId);
        return loadNodeFromDb(rootId);
    }

    @PostConstruct
    public void init() {
        System.out.println("Loading trie from DB...");
        var root = loadTrieFromDb();
        System.out.println("Total nodes loaded from DB: " + this.count);
//        this.trieRoot = root;
        trieService = new TrieService(root);
        System.out.println("Populated the trie");
//        System.out.println("Inserting trie back to DB...");
//        this.deleteAll();
//        this.insertNode(root);
//        System.out.println("Trie insertion complete.");
    }
}
