package com.balazskreith.vcollections;

public class VLinkedListNode<K, V> {
	public K next;
	public K prev;
	public V value;

	public VLinkedListNode() {

	}

	public VLinkedListNode(K prev, K next, V value) {
		this.value = value;
		this.prev = prev;
		this.next = next;
	}
}
