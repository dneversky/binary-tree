package com.dneversky.service;

import java.util.StringJoiner;

public class Node<T> {

    public final T value;
//    public Node<T> parent;
    public Node<T> left;
    public Node<T> right;

    public Node(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .add("left=" + left)
                .add("right=" + right)
                .toString();
    }
}
