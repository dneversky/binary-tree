package com.dneversky.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TreeManager {

    @Autowired
    private SimpMessagingTemplate template;

    private final Map<Class<?>, BinaryTree<?>> trees = new HashMap<>();
    private BinaryTree<Integer> tree;
    private List<Integer> elements = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> BinaryTree<T> getOrCreateTree(Class<T> clazz) {
        return (BinaryTree<T>) trees.computeIfAbsent(clazz, c -> new BinaryTree<>(template, false));
    }

    public void reset() {
        trees.clear();
    }

    public Node getActiveRoot() {
        Optional<Map.Entry<Class<?>, BinaryTree<?>>> activeTree = trees.entrySet().stream()
                .filter(e -> !e.getValue().nodes.isEmpty())
                .findFirst();
        return activeTree.isPresent() ? activeTree.get().getValue().getRoot() : null;
    }

    public List<Integer> getAvailableElements() {
        return elements;
    }

    public double profileInsertIntoTree(int size) {
        tree = new BinaryTree<>(template, true);
        elements.clear();
        Random random = new Random();
        long startInsertion = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            int nextInt = random.nextInt();
            tree.insert(nextInt);
            elements.add(nextInt);
        }
        long finishInsertion = System.currentTimeMillis() - startInsertion;
        return finishInsertion / 1000.0;
    }

    public double profileSearchInTree(int element) {
        long startSearching = System.currentTimeMillis();
        tree.search(element);
        long finishInsertion = System.currentTimeMillis() - startSearching;
        return finishInsertion / 1000.0;
    }
}
