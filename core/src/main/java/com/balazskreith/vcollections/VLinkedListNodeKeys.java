package com.balazskreith.vcollections;

public class VLinkedListNodeKeys<T> {
	public T next;
	public T prev;

	public VLinkedListNodeKeys(T prev, T next) {
		this.prev = prev;
		this.next = next;
	}

	public VLinkedListNodeKeys() {
	}
}
