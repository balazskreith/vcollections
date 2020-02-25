package com.wobserver.vcollections;

import com.wobserver.vcollections.builders.FieldAccessBuilder;
import com.wobserver.vcollections.storages.FieldAccessor;
import com.wobserver.vcollections.storages.IStorage;
import com.wobserver.vcollections.storages.MemoryStorage;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class VLinkedListTest implements ListTest<VLinkedListTest.Node, VLinkedList<UUID, VLinkedListTest.Node>>, DequeTest<VLinkedListTest.Node, VLinkedList<UUID, VLinkedListTest.Node>> {

	public static class Node {
		public UUID prev;
		public UUID next;
		public UUID key;
		public String value;
		
		@Override
		public String toString() {
			return this.value;
		}
		
		@Override
		public boolean equals(Object peer) {
			if (peer == null) {
				return false;
			}
			if (this.value == null) {
				return ((Node) peer).value == null;
			}
			return this.value.equals(((Node) peer).value);
		}
	}

	@Override
	public Node toItem(String item) {
		Node result = new Node();
		result.key = UUID.randomUUID();
		result.value = item;
		return result;
	}

	@Override
	public List<Node> asArrayList(String... items) {
		Node actual;
		Node prev = null;
		List<Node> result = new ArrayList<>();
		for (String item : items) {
			actual = new Node();
			actual.value = item;
			actual.key = UUID.randomUUID();
			if (prev == null) {
				prev = actual;
			} else {
				prev.next = actual.key;
				actual.prev = prev.key;
				prev = actual;
			}
			result.add(actual);
		}
		return result;
	}

	@Override
	public String getValue(Node value) {
		return value.value;
	}

	@Override
	public void setValue(Node item, String value) {
		item.value = value;
	}

	private Node makeNode(UUID uuid, UUID prev, UUID next, String value) {
		Node result = new Node();
		result.value = value;
		result.key = uuid;
		result.next = next;
		result.prev = prev;
		return result;
	}
	
	private VLinkedList<UUID, Node> makeLinkedList(long maxCapacity, String... items) {
		HashMap<UUID, Node> initialItems = new HashMap<>();
		List<UUID> uuids = Stream.iterate(UUID.randomUUID(), i -> UUID.randomUUID()).limit(items.length).collect(Collectors.toList());
		UUID first = null;
		Node last = null;
		if (items != null) {
			for (int i = 0; i < items.length; ++i) {
				String value = items[i];
				Node node;
				if (i == 0 && i == items.length - 1) {
					last = node = makeNode(uuids.get(i), null, null, value);
					first = uuids.get(0);
				} else if (i == 0) {
					last = node = makeNode(uuids.get(i), null, uuids.get(i + 1), value);
					first = uuids.get(0);
				} else if (i == items.length - 1) {
					last = node = makeNode(uuids.get(i), uuids.get(i - 1), null, value);
				} else {
					last = node = makeNode(uuids.get(i), uuids.get(i - 1), uuids.get(i + 1), value);
				}
				
				initialItems.put(uuids.get(i), node);
			}
		}
		IStorage<UUID, Node> storage = new MemoryStorage<>(null, initialItems, maxCapacity);
		VLinkedList<UUID, Node> result;
		FieldAccessor<Node, UUID> prevAccessor = new FieldAccessBuilder<>().withField("prev").withClass(Node.class.getName()).getFieldAccessor();
		FieldAccessor<Node, UUID> keyAccessor = new FieldAccessBuilder<>().withField("key").withClass(Node.class.getName()).getFieldAccessor();
		FieldAccessor<Node, UUID> nextAccessor = new FieldAccessBuilder<>().withField("next").withClass(Node.class.getName()).getFieldAccessor();
		if (first == null) {
			result = new VLinkedList<>(storage,null, null, nextAccessor, prevAccessor, keyAccessor);
		} else {
			result = new VLinkedList<>(storage, first, last.key, nextAccessor, prevAccessor, keyAccessor);
		}
		return result;
	}

	@Override
	public Deque<Node> makeDeque(String... items) {
		return this.makeLinkedList(IStorage.NO_MAX_SIZE, items);
	}

	public List<Node> makeList(String... items) {
		return this.makeLinkedList(IStorage.NO_MAX_SIZE, items);
	}

	
}