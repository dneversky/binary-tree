package com.dneversky.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.LinkedList;
import java.util.StringJoiner;

public class BinaryTree<T> {

    public LinkedList<Node<T>> nodes = new LinkedList<>();
    private final SimpMessagingTemplate template;
    private final boolean enableProfiling;
    private Node<T> root;

    public BinaryTree(SimpMessagingTemplate template, boolean enableProfiling) {
        this.template = template;
        this.enableProfiling = enableProfiling;
    }

    public void insert(T value) {
        if (root == null) {
            insertRoot(value);
        } else {
            insertLeaf(value);
        }
    }

    private void insertRoot(T value) {
        Node<T> node = new Node<>(value);
        this.root = node;
        this.nodes.add(node);
        template.convertAndSend("/process/found" , node);
    }

    private void insertLeaf(T value) {
        Node<T> parent = findParent(value);
        Node<T> node = new Node<>(value);
        render("compare", parent);
        if (isLessThan(value, parent.value)) {
            parent.left = node;
        } else {
            parent.right = node;
        }
        render("found", node);
        this.nodes.add(node);
    }

    private Node<T> findParent(T value) {
        Node<T> current = root;
        while (true) {
            render("compare", current);
            if (isLessThan(value, current.value)) {
                if (current.left == null) {
                    return current;
                } else {
                    current = current.left;
                }
            } else {
                if (current.right == null) {
                    return current;
                } else {
                    current = current.right;
                }
            }
        }
    }

    private void render(String endpoint, Object payload) {
        try {
            if (!enableProfiling) {
                Thread.sleep(3000);
                template.convertAndSend("/process/" + endpoint, payload);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Node<T> getRoot() {
        return root;
    }

    public Node<T> search(T value) {
        return searchRecursive(root, value);
    }

    private Node<T> searchRecursive(Node<T> current, T value) {
        if (current == null) {
            return null;
        }
        if (isLessThan(value, current.value)) {
            return searchRecursive(current.left, value);
        } else if (isGreaterThan(value, current.value)) {
            return searchRecursive(current.right, value);
        } else {
            return current;
        }
    }

    private boolean isLessThan(T value1, T value2) {
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            return ((Comparable<T>) value1).compareTo(value2) < 0;
        }
        throw new UnsupportedOperationException("Values must be Comparable or a custom comparator should be provided.");
    }

    private boolean isGreaterThan(T value1, T value2) {
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            return ((Comparable<T>) value1).compareTo(value2) > 0;
        }
        throw new UnsupportedOperationException("Values must be Comparable or a custom comparator should be provided.");
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BinaryTree.class.getSimpleName() + "[", "]")
                .add(root.toString())
                .toString();
    }
}
