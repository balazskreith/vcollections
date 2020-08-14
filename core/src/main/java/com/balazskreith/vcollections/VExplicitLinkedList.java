package com.balazskreith.vcollections;

import com.balazskreith.vcollections.storages.FieldAccessor;
import com.balazskreith.vcollections.storages.IStorage;
import java.io.Serializable;
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

public class VExplicitLinkedList<K, V> implements List<V>, Deque<V>, Serializable {

	private Node head;
	private Node tail;
	private final FieldAccessor<V, K> nextAccessor;
	private final FieldAccessor<V, K> prevAccessor;
	private final FieldAccessor<V, K> keyAccessor;
	private IStorage<K, V> storage;

	public VExplicitLinkedList(IStorage<K, V> storage, K headKey, K tailKey, FieldAccessor<V, K> nextAccessor, FieldAccessor<V, K> prevAccessor, FieldAccessor<V, K> keyAccessor) {
		this.storage = storage;
		this.nextAccessor = nextAccessor;
		this.prevAccessor = prevAccessor;
		this.keyAccessor = keyAccessor;
		if (headKey != null) {
			this.head = this.load(headKey);
		}
		if (tailKey != null) {
			this.tail = this.load(tailKey);
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
			this.head = this.tail = node = new Node(v);
			this.head.setPrevKey(null);
			this.head.setNextKey(null);
			this.head.save();
			return;
		}

		node = new Node(v);
		node.setNextKey(this.head.getKey());
		node.setPrevKey(null);
		node.save();
		this.head.setPrevKey(node.getKey());
		this.head.save();
		this.head = node;
	}

	@Override
	public void addLast(V v) {
		Node node;
		if (this.tail == null) {
			this.head = this.tail = node = new Node(v);
			this.head.setPrevKey(null);
			this.head.setNextKey(null);
			node.save();
			return;
		}

		node = new Node(v);
		node.setPrevKey(this.tail.getKey());
		node.setNextKey(null);
		node.save();
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
		V first = null;
		V last = null;
		K firstKey = null;
		K lastKey = null;
		for (V value : c) {
			K key = this.keyAccessor.get(value);
			this.storage.update(key, value);
			if (first == null) {
				first = value;
				firstKey = key;
			}
			last = value;
			lastKey = key;
		}
		Node after = this.getNodeAt(index);
		if (after.getPrevKey() == null) {
			// the after is the had
			this.head.setPrevKey(lastKey);
			this.head.save();
			this.nextAccessor.set(last, this.head.getKey());
			this.storage.update(lastKey, last);
			this.head = this.load(lastKey);
		} else {
			Node before = after.getPrevNode();
			before.setNextKey(firstKey);
			this.prevAccessor.set(first, before.getKey());
			this.storage.update(firstKey, first);
			before.save();

			after.setPrevKey(lastKey);
			this.nextAccessor.set(last, after.getKey());
			this.storage.update(lastKey, last);
			after.save();
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
		Node node = new Node(element);
		before.setNextKey(node.getKey());
		node.setPrevKey(before.getKey());

		before.save();
		node.save();
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
			this.actual = VExplicitLinkedList.this.head;
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
				return VExplicitLinkedList.this.size() - 1;
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
					VExplicitLinkedList.this.addFirst(value);
				} else { // we stared, and reached the last
					VExplicitLinkedList.this.addLast(value);
				}
				return;
			}
			if (this.actual.getNextKey() == null) { // last node
				VExplicitLinkedList.this.addLast(value);
				return;
			}
			// we have next, and we have prev
			Node next = this.actual.getNextNode();
			Node node = new Node(value);

			// We insert the new node between the actual and the next
			node.setNextKey(next.getKey());
			node.setPrevKey(this.actual.getKey());

			// we set the next node to the new node
			this.actual.setNextKey(node.getKey());

			// and we set the old next node previous node to the new node
			next.setPrevKey(node.getKey());

			// and we save all
			this.actual.save();
			node.save();
			next.save();
		}
	}

	private abstract class NodeForwardIterator<R> implements Iterator<R> {
		private Node actual;
		private boolean started = false;

		private NodeForwardIterator() {
			this.actual = VExplicitLinkedList.this.head;
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
		V value = this.storage.read(key);
		Node result = new Node(value);
		return result;
	}


	public void unlinkAndSave(Node node) {
		if (node.getNextKey() == null && node.getPrevKey() == null) {
			this.head = VExplicitLinkedList.this.tail = null;
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

	private class Node {

		K key;
		K nextKey;
		K prevKey;
		V value;

		public Node(V value) {
			this.value = value;
			this.load();
		}

		void load() {
			this.key = VExplicitLinkedList.this.keyAccessor.get(value);
			this.nextKey = VExplicitLinkedList.this.nextAccessor.get(value);
			this.prevKey = VExplicitLinkedList.this.prevAccessor.get(value);
		}

		void setValue(V value) {
			this.value = value;
			this.load();
		}

		K getNextKey() {
			return this.nextKey;
		}

		void setNextKey(K key){
			this.nextKey = key;
		}

		K getPrevKey() {
			return this.prevKey;
		}

		void setPrevKey(K key){
			this.prevKey = key;
		}

		Node getNextNode() {
			if (this.nextKey == null) {
				return null;
			}
			V nextValue = VExplicitLinkedList.this.storage.read(this.nextKey);
			return new Node(nextValue);
		}

		Node getPrevNode() {
			if (this.prevKey == null) {
				return null;
			}
			V prevValue = VExplicitLinkedList.this.storage.read(this.prevKey);
			return new Node(prevValue);
		}

		public V getValue() {
			return this.value;
		}

		public void save() {
			VExplicitLinkedList.this.prevAccessor.set(this.value, this.prevKey);
			VExplicitLinkedList.this.nextAccessor.set(this.value, this.nextKey);
			VExplicitLinkedList.this.storage.update(this.key, this.value);
			if (this.key == null) {
				return;
			}
			if (VExplicitLinkedList.this.head != null) {
				if (VExplicitLinkedList.this.head == this) {
					return;
				}
				K headKey = VExplicitLinkedList.this.head.getKey();
				if (this.key.equals(headKey)) {
					VExplicitLinkedList.this.head = this;
				}
			}
			if (VExplicitLinkedList.this.tail != null) {
				if (VExplicitLinkedList.this.tail == this) {
					return;
				}
				K tailKey = VExplicitLinkedList.this.tail.getKey();
				if (this.key.equals(tailKey)) {
					VExplicitLinkedList.this.tail = this;
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
