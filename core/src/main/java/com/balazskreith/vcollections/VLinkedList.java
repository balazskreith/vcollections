package com.balazskreith.vcollections;

import com.balazskreith.vcollections.activeconfigs.VLinkedListActiveConfig;
import com.balazskreith.vcollections.storages.IStorage;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class VLinkedList<K, V> implements List<V>, Deque<V> {

	private Node head;
	private Node tail;
	//	private VLinkedListNode<K, V> head;
//	private VLinkedListNode<K, V> tail;
	private final IStorage<K, VLinkedListNode<K, V>> storage;
	private final VLinkedListActiveConfig<K, V> config;

	public VLinkedList(VLinkedListActiveConfig<K, V> config) {
		this.config = config;
		
		this.storage = config.storageBuilder.build();
		if (config.head != null) {
			this.head = this.load(config.head);
		}
		if (config.tail != null) {
			this.tail = this.load(config.tail);
		}

	}

	@Override
	public int size() {
		return this.storage.entries().intValue();
	}

	@Override
	public boolean isEmpty() {
		return this.storage.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		Node node = this.findFirst(o, null);
		return node != null;
	}

	@Override
	public Iterator<V> iterator() {
		return new NodeForwardIterator<V>() {
			@Override
			public V next() {
				Node node = this.nextNode();
				if (node == null) {
					return null;
				}
				return node.getValue();
			}
		};
	}

	@Override
	public Iterator<V> descendingIterator() {
		return new NodeBackwardIterator<V>() {
			@Override
			public V next() {
				Node node = this.nextNode();
				if (node == null) {
					return null;
				}
				return node.getValue();
			}
		};
	}

	@Override
	public void forEach(Consumer<? super V> action) {
		if (action == null) {
			throw new NullPointerException();
		}
		Node node;
		for (node = this.head; node != null; node = node.getNextNode()) {
			V before = node.getValue();
			V after = before;
			action.accept(before);
			if (before == null) {
				if (after != null) {
					node.setValue(after);
					node.save();
				}
			} else if (!before.equals(after)) {
				node.setValue(after);
				node.save();
			}
		}
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[this.size()];
		int i = 0;
		for (Iterator<V> it = this.iterator(); it.hasNext(); ) {
			V value = it.next();
			result[i++] = value;
		}
		return result;
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		// TODO: implement this.
		return null;
	}

//	@Override
//	public <T1> T1[] toArray(IntFunction<T1[]> generator) {
//		return null;
//	}

	@Override
	public void addFirst(V v) {
		Node node;
		if (this.head == null) {
			this.head = this.tail = this.createAndSave(v, null, null);
			return;
		}


		node = this.createAndSave(v, null, this.head.getKey());
		this.head.setPrevKey(node.getKey());
		this.head.save();
		this.head = node;
	}

	@Override
	public void addLast(V v) {
		Node node;
		if (this.tail == null) {
			this.head = this.tail = this.createAndSave(v, null, null);
			return;
		}

		node = this.createAndSave(v, this.tail.getKey(), null);
		this.tail.setNextKey(node.getKey());
		this.tail.save();
		this.tail = node;
	}

	@Override
	public boolean offerFirst(V v) {
		this.addFirst(v);
		return true;
	}

	@Override
	public boolean offerLast(V v) {
		this.addLast(v);
		return true;
	}

	@Override
	public V removeFirst() {
		if (this.head == null) {
			return null;
		}
		V result = this.head.getValue();
		this.unlinkAndSave(this.head);
		return result;
	}

	@Override
	public V removeLast() {
		if (this.tail == null) {
			return null;
		}
		V result = this.tail.getValue();
		this.unlinkAndSave(this.tail);
		return result;
	}

	@Override
	public V pollFirst() {
		return this.removeFirst();
	}

	@Override
	public V pollLast() {
		return this.removeLast();
	}

	@Override
	public V getFirst() {
		if (this.head == null) {
			return null;
		}
		return this.head.getValue();
	}

	@Override
	public V getLast() {
		if (this.tail == null) {
			return null;
		}
		return this.tail.getValue();
	}

	@Override
	public V peekFirst() {
		return this.getFirst();
	}

	@Override
	public V peekLast() {
		return this.getLast();
	}

	@Override
	public boolean removeFirstOccurrence(Object o) {
		if (this.head == null) {
			return false;
		}
		Node found = this.findFirst(o);
		if (found == null) {
			return false;
		}
		this.unlinkAndSave(found);
		return true;
	}

	@Override
	public boolean removeLastOccurrence(Object o) {
		if (this.tail == null) {
			return false;
		}
		Node found = this.findLast(o);
		if (found == null) {
			return false;
		}
		this.unlinkAndSave(found);
		return true;
	}

	@Override
	public boolean add(V v) {
		this.addLast(v);
		return true;
	}

	@Override
	public boolean offer(V v) {
		this.addLast(v);
		return true;
	}

	@Override
	public V remove() {
		return this.removeFirst();
	}

	@Override
	public V poll() {
		return this.removeFirst();
	}

	@Override
	public V element() {
		return this.getFirst();
	}

	@Override
	public V peek() {
		return this.getFirst();
	}

	@Override
	public boolean remove(Object o) {
		Node found = findFirst(o);
		if (found == null) {
			return false;
		}
		this.unlinkAndSave(found);
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return c.stream().allMatch(item -> this.findFirst(item) != null);
	}

	@Override
	public boolean addAll(Collection<? extends V> c) {
		c.forEach(item -> this.add(item));
		return true;
	}

	@Override
	public void push(V v) {
		this.add(v);
	}

	@Override
	public V pop() {
		return this.removeFirst();
	}

	@Override
	public boolean addAll(int index, Collection<? extends V> c) {
		if (c == null || c.size() < 1) {
			return false;
		}
		if (index < 0 || this.size() <= index) {
			throw new IndexOutOfBoundsException();
		}
		Node firstInsertedNode = null;
		Node lastInsertedNode = null;
		V first = null;
		V last = null;
		K firstKey = null;
		K lastKey = null;
		for (V value : c) {
			if (firstInsertedNode == null) {
				lastInsertedNode = firstInsertedNode = this.createAndSave(value, null, null);
				continue;
			}
			Node insertedNode = this.createAndSave(value, lastInsertedNode.getKey(), null);
			lastInsertedNode.setNextKey(insertedNode.getKey());
			lastInsertedNode.save();
			lastInsertedNode = insertedNode;
		}
		Node after = this.getNodeAt(index);
		if (after.getPrevKey() == null) {
			// the after is the head
			this.head.setPrevKey(lastInsertedNode.getKey());
			this.head.save();
			lastInsertedNode.setNextKey(this.head.getKey());
			lastInsertedNode.save();
			this.head = firstInsertedNode;
		} else {
			Node before = after.getPrevNode();
			before.setNextKey(firstInsertedNode.getKey());
			before.save();
			firstInsertedNode.setPrevKey(before.getKey());
			firstInsertedNode.save();

			after.setPrevKey(lastInsertedNode.getKey());
			after.save();
			lastInsertedNode.setNextKey(after.getKey());
			lastInsertedNode.save();
		}

		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (Object item : c) {
			Node node = this.findFirst(item);
			if (node == null) {
				continue;
			}
			this.unlinkAndSave(node);
			result = true;
		}
		return result;
	}

	@Override
	public boolean removeIf(Predicate<? super V> filter) {
		if (filter == null) {
			throw new NullPointerException();
		}
		if (this.head == null) {
			return false;
		}
		Node node = this.head;
		boolean result = false;
		while (true) {
			if (node == null) {
				break;
			}
			V value = node.getValue();
			if (!filter.test(value)) {
				node = node.getNextNode();
				continue;
			}

			Node next = null;
			if (node.getNextKey() != null) {
				next = node.getNextNode();
			}
			this.unlinkAndSave(node);
			result = true;
			node = next;
		}

		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.removeIf(value -> !c.contains(value));
	}

	@Override
	public void replaceAll(UnaryOperator<V> operator) {
		for (Node node = this.head; node != null; node = node.getNextNode()) {
			V value = node.getValue();
			value = operator.apply(value);
			node.setValue(value);
			node.save();
		}
	}

	@Override
	public void sort(Comparator<? super V> c) {

	}

	@Override
	public void clear() {
		this.storage.clear();
		this.head = this.tail = null;
	}

	@Override
	public V get(int index) {
		Node node = this.getNodeAt(index);
		if (node == null) {
			return null;
		}
		return node.getValue();
	}

	@Override
	public V set(int index, V element) {
		Node node = this.getNodeAt(index);
		K prevKeyBefore = node.getPrevKey();
		K nextKeyBefore = node.getNextKey();
		K keyBefore = node.getKey();
		V result = node.getValue();
		node.setValue(element);
		if (node.getPrevKey() != prevKeyBefore) {
			throw new IllegalStateException("You cannot change the chain of node with Set operation. Prev key was: " + prevKeyBefore.toString() + " after set it is: " + node.getPrevKey().toString());
		}

		if (node.getNextKey() != nextKeyBefore) {
			throw new IllegalStateException("You cannot change the chain of node with Set operation. next key  was: " + nextKeyBefore.toString() + " after set it is: " + node.getNextKey().toString());
		}

		if (node.getKey() != keyBefore) {
			throw new IllegalStateException("You cannot change the chain of node with Set operation. key was: " + keyBefore.toString() + " after set it is: " + node.getKey().toString());
		}

		node.save();
		return result;
	}

	@Override
	public void add(int index, V element) {
		Node before = this.getNodeAt(index);
		if (before.getNextKey() == null) {
			this.addLast(element);
			return;
		}
		Node node = this.createAndSave(element, before.getKey(), before.getNextKey());
		before.setNextKey(node.getKey());
		before.save();
		Node after = before.getNextNode();
		after.setPrevKey(node.getKey());
		after.save();
	}

	@Override
	public V remove(int index) {
		Node node = this.getNodeAt(index);
		V result = node.getValue();
		this.unlinkAndSave(node);
		return result;
	}

	@Override
	public int indexOf(Object o) {
		AtomicReference<Long> positionHolder = new AtomicReference<>();
		this.findFirst(o, positionHolder);
		return positionHolder.get().intValue();
	}

	@Override
	public int lastIndexOf(Object o) {
		AtomicReference<Long> positionHolder = new AtomicReference<>();
		this.findLast(o, positionHolder);
		return positionHolder.get().intValue();
	}

	@Override
	public ListIterator<V> listIterator() {
		return new NodeForwardListiterator();
	}

	@Override
	public ListIterator<V> listIterator(int index) {
		return new NodeForwardListiterator(this.getNodeAt(index), index);
	}

	@Override
	public List<V> subList(int fromIndex, int toIndex) {
		List<V> result = new LinkedList<>();
		Node node = this.getNodeAt(fromIndex);
		if (node == null) {
			return result;
		}
		for (int i = fromIndex; i < toIndex; ++i) {
			result.add(node.getValue());
			node = node.getNextNode();
		}
		return result;
	}

	@Override
	public Spliterator<V> spliterator() {
		return null;
	}

	@Override
	public Stream<V> stream() {
		return null;
	}

	@Override
	public Stream<V> parallelStream() {
		return null;
	}


	private class NodeForwardListiterator implements ListIterator<V> {
		private boolean started = false;
		private Node actual;
		private Long position;

		private NodeForwardListiterator() {
			this.actual = VLinkedList.this.head;
			this.position = 0L;
		}

		private NodeForwardListiterator(Node node, long position) {
			this.actual = node;
			this.position = position;
		}


		@Override
		public boolean hasNext() {
			if (!this.started) {
				return this.actual != null;
			}
			return this.actual.getNextKey() != null;
		}

		public Node nextNode() {
			if (!this.started) {
				this.started = true;
				return this.actual;
			}
			this.actual = this.actual.getNextNode();
			++this.position;
			return this.actual;
		}

		@Override
		public boolean hasPrevious() {
			if (!this.started) {
				return this.actual != null;
			}
			return this.actual.getPrevKey() != null;
		}

		public Node prevNode() {
			if (!this.started) {
				this.started = true;
				return this.actual;
			}
			this.actual = this.actual.getPrevNode();
			--this.position;
			return this.actual;
		}

		@Override
		public V previous() {
			Node node = this.prevNode();
			if (node == null) {
				return null;
			}
			return node.getValue();
		}

		@Override
		public V next() {
			Node node = this.nextNode();
			if (node == null) {
				return null;
			}
			return node.getValue();
		}

		@Override
		public int nextIndex() {
			if (!this.started) {
				return 0;
			}
			return this.position.intValue() + 1;
		}

		@Override
		public int previousIndex() {
			if (!this.started) {
				return VLinkedList.this.size() - 1;
			}
			return this.position.intValue() - 1;
		}

		@Override
		public void remove() {

		}

		@Override
		public void set(V value) {
			if (this.actual == null) {
				return;
			}
			this.actual.setValue(value);
			this.actual.save();
		}

		@Override
		public void add(V value) {
			if (this.actual == null) {
				if (!this.started) { // we have not started it, so this list is empty
					VLinkedList.this.addFirst(value);
				} else { // we stared, and reached the last
					VLinkedList.this.addLast(value);
				}
				return;
			}
			if (this.actual.getNextKey() == null) { // last node
				VLinkedList.this.addLast(value);
				return;
			}
			// we have next, and we have prev
			Node next = this.actual.getNextNode();
			// We insert the new node between the actual and the next
			Node node = VLinkedList.this.createAndSave(value, this.actual.getKey(), next.getKey());

			// we set the next node to the new node
			this.actual.setNextKey(node.getKey());

			// and we set the old next node previous node to the new node
			next.setPrevKey(node.getKey());

			// and we save all
			this.actual.save();
//			node.save(); // we already saved it.
			next.save();
		}
	}

	private abstract class NodeForwardIterator<R> implements Iterator<R> {
		private Node actual;
		private boolean started = false;

		private NodeForwardIterator() {
			this.actual = VLinkedList.this.head;
		}

		@Override
		public boolean hasNext() {
			if (this.actual == null) {
				return false;
			}
			return this.actual.getNextKey() != null;
		}

		public Node nextNode() {
			if (!this.started) {
				this.started = true;
				return this.actual;
			}
			this.actual = this.actual.getNextNode();
			return this.actual;
		}

		public abstract R next();
	}

	private abstract class NodeBackwardIterator<R> implements Iterator<R> {
		private Node actual;
		private boolean started = false;

		private NodeBackwardIterator() {

		}

		@Override
		public boolean hasNext() {
			if (this.actual == null) {
				return false;
			}
			return this.actual.getPrevKey() != null;
		}

		public Node nextNode() {
			if (!this.started) {
				this.started = true;
				return this.actual;
			}
			this.actual = this.actual.getPrevNode();
			return this.actual;
		}

		public abstract R next();
	}


	private Function<Node, Boolean> getTesterFor(Object obj) {
		if (obj == null) {
			return node -> node.getValue() == null;
		} else {
			return node -> obj.equals(node.getValue());
		}
	}

	private Node getNodeAt(long position) {
		if (this.storage.entries() <= position) {
			throw new IndexOutOfBoundsException("The requested position " + position + " is out of the list boundary: " + this.storage.entries());
		}
		for (Node node = this.head; node != null; node = node.getNextNode(), --position) {
			if (position == 0) {
				return node;
			}
		}
		return null;
	}

	private Node findFirst(Object o) {
		return this.findFirst(o, null);
	}

	private Node findFirst(Object o, AtomicReference<Long> positionHolder) {
		if (this.head == null) {
			return null;
		}
		long position = 0L;
		Node result = null;
		Function<Node, Boolean> tester = this.getTesterFor(o);
		for (Node node = this.head; node != null; node = node.getNextNode(), ++position) {
			if (tester.apply(node)) {
				result = node;
				break;
			}
		}
		if (positionHolder != null) {
			if (result != null) {
				positionHolder.set(position);
			} else {
				positionHolder.set(-1L);
			}
		}
		return result;
	}

	private Node findLast(Object o) {
		return findLast(o, null);
	}

	private Node findLast(Object o, AtomicReference<Long> positionHolder) {
		if (this.tail == null) {
			return null;
		}
		long position = this.storage.entries() - 1L;
		Node result = null;
		Function<Node, Boolean> tester = this.getTesterFor(o);
		for (Node node = this.tail; node != null; node = node.getPrevNode(), --position) {
			if (tester.apply(node)) {
				result = node;
				break;
			}
		}
		if (positionHolder != null) {
			if (result != null) {
				positionHolder.set(position);
			} else {
				positionHolder.set(-1L);
			}
		}
		return result;
	}

	private Node load(K key) {
		if (this.head != null && this.head.getKey().equals(key)) {
			return this.head;
		}
		if (this.tail != null && this.tail.getKey().equals(key)) {
			return this.tail;
		}
		VLinkedListNode<K, V> linkedListNode = this.storage.read(key);
		Node result = new Node(key, linkedListNode);
		return result;
	}


	public void unlinkAndSave(Node node) {
		if (node.getNextKey() == null && node.getPrevKey() == null) {
			this.head = VLinkedList.this.tail = null;
			this.storage.delete(node.getKey());
			return;
		}
		Node prev;
		Node next;
		if (node.getNextKey() == null) {
			prev = node.getPrevNode();
			prev.setNextKey(null);
			prev.save();
			this.storage.delete(node.getKey());
			this.tail = prev;
			return;
		} else {
			next = node.getNextNode();
		}

		if (node.getPrevKey() == null) {
			next = node.getNextNode();
			next.setPrevKey(null);
			next.save();
			this.storage.delete(node.getKey());
			this.head = next;
			return;
		} else {
			prev = node.getPrevNode();
		}

		next.setPrevKey(prev.getKey());
		prev.setNextKey(next.getKey());
		prev.save();
		next.save();
		this.storage.delete(node.getKey());
	}

	public void linkAfter(Node node) {

	}

	public void linkBefore(Node node) {

	}

	private Node createAndSave(V value, K prev, K next) {
		VLinkedListNode<K, V> linkedListNode = new VLinkedListNode<>(prev, next, value);
		K key = this.storage.create(linkedListNode);
		Node result = new Node(key, linkedListNode);
		return result;
	}

	private class Node extends VLinkedListNode<K, V> {

		K key;

		//		K nextKey;
//		K prevKey;
//		V value;

		public Node(K key, VLinkedListNode<K, V> node) {
			super(node.prev, node.next, node.value);
			this.key = key;
		}

		public Node(K key) {
			this.key = key;
			this.load();
		}

		void load() {
			VLinkedListNode<K, V> linkedListNode = VLinkedList.this.storage.read(this.key);
			this.prev = linkedListNode.prev;
			this.next = linkedListNode.next;
			this.value = linkedListNode.value;
		}

		void setValue(V value) {
			this.value = value;
			this.load();
		}

		K getNextKey() {
			return this.next;
		}

		void setNextKey(K key) {
			this.next = key;
		}

		K getPrevKey() {
			return this.prev;
		}

		void setPrevKey(K key) {
			this.prev = key;
		}

		Node getNextNode() {
			if (this.next == null) {
				return null;
			}
			return new Node(this.next);
		}

		Node getPrevNode() {
			if (this.prev == null) {
				return null;
			}
			return new Node(this.prev);
		}

		public V getValue() {
			return this.value;
		}

		public void save() {
			if (this.key == null) {
				return;
			}
			VLinkedList.this.storage.update(this.key, this);
			if (VLinkedList.this.head != null) {
				if (VLinkedList.this.head == this) {
					return;
				}
				K headKey = VLinkedList.this.head.getKey();
				if (this.key.equals(headKey)) {
					VLinkedList.this.head = this;
				}
			}
			if (VLinkedList.this.tail != null) {
				if (VLinkedList.this.tail == this) {
					return;
				}
				K tailKey = VLinkedList.this.tail.getKey();
				if (this.key.equals(tailKey)) {
					VLinkedList.this.tail = this;
				}
			}
		}

		K getKey() {
			return this.key;
		}

	}

	@Override
	public String toString() {
		return "something";
	}

}
