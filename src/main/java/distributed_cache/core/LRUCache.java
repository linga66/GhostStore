package distributed_cache.core;

import java.util.concurrent.ConcurrentHashMap;

public class LRUCache<K, V> {
    private record CacheEntry<V>(V value, long expiryTime) {}
    private static class Node<K, V> {
        K key; CacheEntry<V> entry; Node<K, V> prev, next;
        Node(K key, CacheEntry<V> entry) { this.key = key; this.entry = entry; }
    }

    private final int capacity;
    private final ConcurrentHashMap<K, Node<K, V>> map = new ConcurrentHashMap<>();
    private Node<K, V> head, tail;

    public LRUCache(int capacity) { this.capacity = capacity; }

    public synchronized V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null || System.currentTimeMillis() > node.entry.expiryTime) {
            if (node != null) remove(key);
            return null;
        }
        moveToHead(node);
        return node.entry.value;
    }

    public synchronized void put(K key, V value, long ttlSec) {
        CacheEntry<V> entry = new CacheEntry<>(value, System.currentTimeMillis() + (ttlSec * 1000));
        if (map.containsKey(key)) {
            Node<K, V> node = map.get(key);
            node.entry = entry;
            moveToHead(node);
        } else {
            if (map.size() >= capacity) remove(tail.key);
            Node<K, V> newNode = new Node<>(key, entry);
            addNode(newNode);
            map.put(key, newNode);
        }
    }

    private void remove(K key) {
        Node<K, V> node = map.remove(key);
        if (node != null) removeNode(node);
    }

    private void addNode(Node<K, V> node) {
        node.next = head;
        if (head != null) head.prev = node;
        head = node;
        if (tail == null) tail = head;
    }

    private void removeNode(Node<K, V> node) {
        if (node.prev != null) node.prev.next = node.next; else head = node.next;
        if (node.next != null) node.next.prev = node.prev; else tail = node.prev;
    }

    private void moveToHead(Node<K, V> node) { removeNode(node); addNode(node); }
}