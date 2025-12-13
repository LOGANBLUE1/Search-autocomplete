package com.example.demo.util;

public class Database {
    int count = 0;

//    public void insertIntoDb(Node root) {
//        System.out.println(insertNode(root, mongo));
//    }
//
//    public ObjectId insertNode(Node node, MongoService mongo) {
//        if (node == null) return null;
//
//        // First insert children and collect their ObjectIds
//        List<Object> childrenIds = new ArrayList<>(26);
//        for (int i = 0; i < 26; i++) {
//            Node child = node.children[i];
//            if (child == null) {
//                childrenIds.add(null);
//            } else {
//                ObjectId childId = insertNode(child, mongo); // recursive bottom-up insert
//                childrenIds.add(childId);
//            }
//        }
//
//        // topKFreq -> list of documents
//        List<Document> top = new ArrayList<>();
//        if (node.topKFreq != null) {
//            for (WordFreq wf : node.topKFreq) {
//                top.add(new Document("word", wf.word).append("freq", wf.freq));
//            }
//        }
//
//        Document doc = new Document()
//                .append("isTerminal", node.isTerminal)
//                .append("freq", node.freq)
//                .append("word", node.word)
//                .append("topKFreq", top)
//                .append("children", childrenIds);
//
//        InsertOneResult res = mongo.insert(doc);
//        return res.getInsertedId().asObjectId().getValue();
//    }
}
