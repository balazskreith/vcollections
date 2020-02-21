//package com.wobserver.vcollections;
//
//import com.wobserver.vcollections.storages.IStorage;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.*;
//import java.util.stream.Stream;
//
//public class VLinkedList3<K, V> implements List<V>, Deque<V> {
//
//	private Node head;
//	private Node tail;
//	private final Function<V, K> keyGetter;
//	private final Function<V, K> nextGetter;
//	private final Function<V, K> prevGetter;
//	private final BiConsumer<V, K> nextSetter;
//	private final BiConsumer<V, K> prevSetter;
//	private IStorage<K, V> storage;
//
//	public VLinkedList3(IStorage<K, V> storage, K headUUID, K tailUUID, Function<V, K> keyGetter, Function<V, K> nextGetter, Function<V, K> prevGetter, BiConsumer<V, K> nextSetter, BiConsumer<V, K> prevSetter) {
//		this.storage = storage;
//		this.head = this.load(headUUID);
//		this.tail = this.load(tailUUID);
//		this.keyGetter = keyGetter;
//		this.nextGetter = nextGetter;
//		this.prevGetter = prevGetter;
//		this.nextSetter = nextSetter;
//		this.prevSetter = prevSetter;
//	}
//
//	@Override
//	public int size() {
//		return this.storage.entries().intValue();
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return this.storage.isEmpty();
//	}
//
//	@Override
//	public boolean contains(Object o) {
//		Node node = this.findFirst(o, null);
//		return node != null;
//	}
//
//	@Override
//	public Iterator<V> iterator() {
//		return new NodeForwardIterator<V>() {
//			@Override
//			public V next() {
//				Node node = this.nextNode();
//				if (node == null) {
//					return null;
//				}
//				return node.getValue();
//			}
//		};
//	}
//
//	@Override
//	public Iterator<V> descendingIterator() {
//		return new NodeBackwardIterator<V>() {
//			@Override
//			public V next() {
//				Node node = this.nextNode();
//				if (node == null) {
//					return null;
//				}
//				return node.getValue();
//			}
//		};
//	}
//
//	@Override
//	public void forEach(Consumer<? super V> action) {
//		if (action == null) {
//			throw new NullPointerException();
//		}
//		Node node;
//		for (node = this.head; node != null; node = node.getNextNode()) {
//			V before = node.getValue();
//			action.accept(before);
//			V after = node.getValue();
//			if (before == null) {
//				if (after != null) {
//					node.value = after;
//					node.save();
//				}
//			} else if (!before.equals(after)) {
//				node.value = after;
//				node.save();
//			}
//		}
//	}
//
//	@Override
//	public Object[] toArray() {
//		Object[] result = new Object[this.size()];
//		int i = 0;
//		for (Iterator<V> it = this.iterator(); it.hasNext(); ) {
//			V value = it.next();
//			result[i++] = value;
//		}
//		return result;
//	}
//
//	@Override
//	public <T1> T1[] toArray(T1[] a) {
//		// TODO: implement this.
//		return null;
//	}
//
////	@Override
////	public <T1> T1[] toArray(IntFunction<T1[]> generator) {
////		return null;
////	}
//
//	@Override
//	public void addFirst(V v) {
//		Node node;
//		if (this.head == null) {
//			this.head = this.tail = node = new Node(null, null, v);
//			node.save();
//			return;
//		}
//		node = new Node(null, this.head.getKey(), v);
//		this.head.prevKey = node.getKey();
//		this.head.save();
//		this.head = node;
//	}
//
//	@Override
//	public void addLast(V v) {
//		Node node;
//		if (this.tail == null) {
//			this.head = this.tail = node = new Node(null, null, v);
//			node.save();
//			return;
//		}
//
//		node = new Node(this.tail.getKey(), null, v);
//		node.save();
//		this.tail.nextKey = node.getKey();
//		this.tail.save();
//		this.tail = node;
//	}
//
//	@Override
//	public boolean offerFirst(V v) {
//		this.addFirst(v);
//		return true;
//	}
//
//	@Override
//	public boolean offerLast(V v) {
//		this.addLast(v);
//		return true;
//	}
//
//	@Override
//	public V removeFirst() {
//		if (this.head == null) {
//			return null;
//		}
//		V result = this.head.getValue();
//		this.unlinkAndSave(this.head);
//		return result;
//	}
//
//	@Override
//	public V removeLast() {
//		if (this.tail == null) {
//			return null;
//		}
//		V result = this.tail.getValue();
//		this.unlinkAndSave(this.tail);
//		return result;
//	}
//
//	@Override
//	public V pollFirst() {
//		return this.removeFirst();
//	}
//
//	@Override
//	public V pollLast() {
//		return this.removeLast();
//	}
//
//	@Override
//	public V getFirst() {
//		if (this.head == null) {
//			return null;
//		}
//		return this.head.getValue();
//	}
//
//	@Override
//	public V getLast() {
//		if (this.tail == null) {
//			return null;
//		}
//		return this.tail.getValue();
//	}
//
//	@Override
//	public V peekFirst() {
//		return this.getFirst();
//	}
//
//	@Override
//	public V peekLast() {
//		return this.getLast();
//	}
//
//	@Override
//	public boolean removeFirstOccurrence(Object o) {
//		if (this.head == null) {
//			return false;
//		}
//		Node found = this.findFirst(o);
//		if (found == null) {
//			return false;
//		}
//		this.unlinkAndSave(found);
//		return true;
//	}
//
//	@Override
//	public boolean removeLastOccurrence(Object o) {
//		if (this.tail == null) {
//			return false;
//		}
//		Node found = this.findLast(o);
//		if (found == null) {
//			return false;
//		}
//		this.unlinkAndSave(found);
//		return true;
//	}
//
//	@Override
//	public boolean add(V v) {
//		this.addLast(v);
//		return true;
//	}
//
//	@Override
//	public boolean offer(V v) {
//		this.addLast(v);
//		return true;
//	}
//
//	@Override
//	public V remove() {
//		return this.removeFirst();
//	}
//
//	@Override
//	public V poll() {
//		return this.removeFirst();
//	}
//
//	@Override
//	public V element() {
//		return this.getFirst();
//	}
//
//	@Override
//	public V peek() {
//		return this.getFirst();
//	}
//
//	@Override
//	public boolean remove(Object o) {
//		Node found = findFirst(o);
//		if (found == null) {
//			return false;
//		}
//		this.unlinkAndSave(found);
//		return true;
//	}
//
//	@Override
//	public boolean containsAll(Collection<?> c) {
//		return c.stream().allMatch(item -> this.findFirst(item) != null);
//	}
//
//	@Override
//	public boolean addAll(Collection<? extends V> c) {
//		c.forEach(item -> this.add(item));
//		return true;
//	}
//
//	@Override
//	public void push(V v) {
//		this.add(v);
//	}
//
//	@Override
//	public V pop() {
//		return this.removeFirst();
//	}
//
//	@Override
//	public boolean addAll(int index, Collection<? extends V> c) {
//		if (c == null || c.size() < 1) {
//			return false;
//		}
//		if (index < 0 || this.size() <= index) {
//			throw new IndexOutOfBoundsException();
//		}
//		Node after = this.getNode(index);
//		Node first = null;
//		Node last = null;
//		for (V value : c) {
//			if (first == null) {
//				first = last = new Node(null, null, value);
//				continue;
//			}
//			Node node = new Node(last.key, null, value);
//			last.nextKey = node.key;
//			last.save();
//			last = node;
//		}
//
//		last.nextKey = after.key;
//		last.save();
//
//		if (after == this.head) {
//			this.head = first;
//		} else {
//			Node before = after.getPrevNode();
//			before.nextKey = first.key;
//			before.save();
//			first.prevKey = before.key;
//			first.save();
//		}
//
//		after.prevKey = last.key;
//		after.save();
//		return true;
//	}
//
//	@Override
//	public boolean removeAll(Collection<?> c) {
//		boolean result = false;
//		for (Object item : c) {
//			Node node = this.findFirst(item);
//			if (node == null) {
//				continue;
//			}
//			this.unlinkAndSave(node);
//			result = true;
//		}
//		return result;
//	}
//
//	@Override
//	public boolean removeIf(Predicate<? super V> filter) {
//		if (filter == null) {
//			throw new NullPointerException();
//		}
//		if (this.head == null) {
//			return false;
//		}
//		Node node = this.head;
//		boolean result = false;
//		while (true) {
//			if (node == null) {
//				break;
//			}
//			V value = node.getValue();
//			if (!filter.test(value)) {
//				node = node.getNextNode();
//				continue;
//			}
//
//			Node next = null;
//			if (node.getNextKey() != null) {
//				next = node.getNextNode();
//			}
//			this.unlinkAndSave(node);
//			result = true;
//			node = next;
//		}
//
//		return result;
//	}
//
//	@Override
//	public boolean retainAll(Collection<?> c) {
//		return this.removeIf(value -> !c.contains(value));
//	}
//
//	@Override
//	public void replaceAll(UnaryOperator<V> operator) {
//		for (Node node = this.head; node != null; node = node.getNextNode()) {
//			V value = node.getValue();
//			value = operator.apply(value);
//			node.value = (value);
//			node.save();
//		}
//	}
//
//	@Override
//	public void sort(Comparator<? super V> c) {
//
//	}
//
//	@Override
//	public void clear() {
//		this.storage.clear();
//		this.head = this.tail = null;
//	}
//
//	@Override
//	public V get(int index) {
//		Node node = this.getNode(index);
//		if (node == null) {
//			return null;
//		}
//		return node.getValue();
//	}
//
//	@Override
//	public V set(int index, V element) {
//		Node node = this.getNode(index);
//		V result = node.getValue();
//		node.value = (element);
//		node.save();
//		return result;
//	}
//
//	@Override
//	public void add(int index, V element) {
//		Node before = this.getNode(index);
//		Node node = new Node(before.getKey(), before.getNextKey(), element);
//		before.nextKey = (node.getNextKey());
//		before.save();
//		node.save();
//	}
//
//	@Override
//	public V remove(int index) {
//		Node node = this.getNode(index);
//		V result = node.value;
//		this.unlinkAndSave(node);
//		return result;
//	}
//
//	@Override
//	public int indexOf(Object o) {
//		AtomicReference<Long> positionHolder = new AtomicReference<>();
//		this.findFirst(o, positionHolder);
//		return positionHolder.get().intValue();
//	}
//
//	@Override
//	public int lastIndexOf(Object o) {
//		AtomicReference<Long> positionHolder = new AtomicReference<>();
//		this.findLast(o, positionHolder);
//		return positionHolder.get().intValue();
//	}
//
//	@Override
//	public ListIterator<V> listIterator() {
//		return new NodeForwardListiterator();
//	}
//
//	@Override
//	public ListIterator<V> listIterator(int index) {
//		return new NodeForwardListiterator(this.getNode(index), index);
//	}
//
//	@Override
//	public List<V> subList(int fromIndex, int toIndex) {
//		List<V> result = new LinkedList<>();
//		Node node = this.getNode(fromIndex);
//		if (node == null) {
//			return result;
//		}
//		for (int i = fromIndex; i < toIndex; ++i) {
//			result.add(node.getValue());
//			node = node.getNextNode();
//		}
//		return result;
//	}
//
//	@Override
//	public Spliterator<V> spliterator() {
//		return null;
//	}
//
//	@Override
//	public Stream<V> stream() {
//		return null;
//	}
//
//	@Override
//	public Stream<V> parallelStream() {
//		return null;
//	}
//
//
//	private class NodeForwardListiterator implements ListIterator<V> {
//		private boolean started = false;
//		private Node actual;
//		private Long position;
//
//		private NodeForwardListiterator() {
//			this.actual = VLinkedList3.this.head;
//			this.position = 0L;
//		}
//
//		private NodeForwardListiterator(Node node, long position) {
//			this.actual = node;
//			this.position = position;
//		}
//
//
//		@Override
//		public boolean hasNext() {
//			if (!this.started) {
//				return this.actual != null;
//			}
//			return this.actual.getNextKey() != null;
//		}
//
//		public Node nextNode() {
//			if (!this.started) {
//				this.started = true;
//				return this.actual;
//			}
//			this.actual = this.actual.getNextNode();
//			++this.position;
//			return this.actual;
//		}
//
//		@Override
//		public boolean hasPrevious() {
//			if (!this.started) {
//				return this.actual != null;
//			}
//			return this.actual.getPrevKey() != null;
//		}
//
//		public Node prevNode() {
//			if (!this.started) {
//				this.started = true;
//				return this.actual;
//			}
//			this.actual = this.actual.getPrevNode();
//			--this.position;
//			return this.actual;
//		}
//
//		@Override
//		public V previous() {
//			Node node = this.prevNode();
//			if (node == null) {
//				return null;
//			}
//			return node.getValue();
//		}
//
//		@Override
//		public V next() {
//			Node node = this.nextNode();
//			if (node == null) {
//				return null;
//			}
//			return node.getValue();
//		}
//
//		@Override
//		public int nextIndex() {
//			if (!this.started) {
//				return 0;
//			}
//			return this.position.intValue() + 1;
//		}
//
//		@Override
//		public int previousIndex() {
//			if (!this.started) {
//				return VLinkedList3.this.size() - 1;
//			}
//			return this.position.intValue() - 1;
//		}
//
//		@Override
//		public void remove() {
//
//		}
//
//		@Override
//		public void set(V value) {
//			if (this.actual == null) {
//				return;
//			}
//			this.actual.value = (value);
//			this.actual.save();
//		}
//
//		@Override
//		public void add(V value) {
//			if (this.actual == null) {
//				if (!this.started) { // we have not started it, so this list is empty
//					VLinkedList3.this.addFirst(value);
//				} else { // we stared, and rerached the last
//					VLinkedList3.this.addLast(value);
//				}
//				return;
//			}
//			if (this.actual.getNextKey() == null) { // last node
//				VLinkedList3.this.addLast(value);
//				return;
//			}
//			// we have next, and we have prev
//			Node next = this.actual.getNextNode();
//			Node node = new Node(this.actual.getKey(), this.actual.getNextKey(), value);
//			this.actual.nextKey = (node.getKey());
//			next.prevKey = (node.getKey());
//
//			node.save();
//			this.actual.save();
//			next.save();
//		}
//	}
//
//	private abstract class NodeForwardIterator<R> implements Iterator<R> {
//		private Node actual;
//		private boolean started = false;
//
//		private NodeForwardIterator() {
//			this.actual = VLinkedList3.this.head;
//		}
//
//		@Override
//		public boolean hasNext() {
//			if (this.actual == null) {
//				return false;
//			}
//			return this.actual.getNextKey() != null;
//		}
//
//		public Node nextNode() {
//			if (!this.started) {
//				this.started = true;
//				return this.actual;
//			}
//			this.actual = this.actual.getNextNode();
//			return this.actual;
//		}
//
//		public abstract R next();
//	}
//
//	private abstract class NodeBackwardIterator<R> implements Iterator<R> {
//		private Node actual;
//		private boolean started = false;
//
//		private NodeBackwardIterator() {
//
//		}
//
//		@Override
//		public boolean hasNext() {
//			if (this.actual == null) {
//				return false;
//			}
//			return this.actual.getPrevKey() != null;
//		}
//
//		public Node nextNode() {
//			if (!this.started) {
//				this.started = true;
//				return this.actual;
//			}
//			this.actual = this.actual.getPrevNode();
//			return this.actual;
//		}
//
//		public abstract R next();
//	}
//
//
//	private Function<Node, Boolean> getTesterFor(Object obj) {
//		if (obj == null) {
//			return node -> node.getValue() == null;
//		} else {
//			return node -> obj.equals(node.getValue());
//		}
//	}
//
//	private Node getNode(long position) {
//		if (this.storage.entries() <= position) {
//			throw new IndexOutOfBoundsException("The requested position " + position + " is out of the list boundary: " + this.storage.entries());
//		}
//		for (Node node = this.head; node != null; node = node.getNextNode(), --position) {
//			if (position == 0) {
//				return node;
//			}
//		}
//		return null;
//	}
//
//	private Node findFirst(Object o) {
//		return this.findFirst(o, null);
//	}
//
//	private Node findFirst(Object o, AtomicReference<Long> positionHolder) {
//		if (this.head == null) {
//			return null;
//		}
//		long position = 0L;
//		Node result = null;
//		Function<Node, Boolean> tester = this.getTesterFor(o);
//		for (Node node = this.head; node != null; node = node.getNextNode(), ++position) {
//			if (tester.apply(node)) {
//				result = node;
//				break;
//			}
//		}
//		if (positionHolder != null) {
//			if (result != null) {
//				positionHolder.set(position);
//			} else {
//				positionHolder.set(-1L);
//			}
//		}
//		return result;
//	}
//
//	private Node findLast(Object o) {
//		return findLast(o, null);
//	}
//
//	private Node findLast(Object o, AtomicReference<Long> positionHolder) {
//		if (this.tail == null) {
//			return null;
//		}
//		long position = this.storage.entries() - 1L;
//		Node result = null;
//		Function<Node, Boolean> tester = this.getTesterFor(o);
//		for (Node node = this.tail; node != null; node = node.getPrevNode(), --position) {
//			if (tester.apply(node)) {
//				result = node;
//				break;
//			}
//		}
//		if (positionHolder != null) {
//			if (result != null) {
//				positionHolder.set(position);
//			} else {
//				positionHolder.set(-1L);
//			}
//		}
//		return result;
//	}
//
//	private Node load(K uuid) {
//		if (this.head != null && this.head.key == uuid) {
//			return this.head;
//		}
//		if (this.tail != null && this.tail.key == uuid) {
//			return this.tail;
//		}
//		Node result = new Node();
//		V value = this.storage.read(uuid);
//		result.key = uuid;
//		result.nextKey = this.nextGetter.apply(value);
//		result.prevKey = this.prevGetter.apply(value);
//		result.value = value;
//		return result;
//	}
//
//
//	public void unlinkAndSave(Node node) {
//		if (node.nextKey == null && node.prevKey == null) {
//			this.head = VLinkedList3.this.tail = null;
//			this.storage.delete(node.key);
//			return;
//		}
//		Node prev;
//		Node next;
//		if (node.nextKey == null) {
//			prev = node.getPrevNode();
//			prev.nextKey = null;
//			prev.save();
//			node.prevKey = null;
//			this.storage.delete(node.key);
//			this.tail = prev;
//			return;
//		} else {
//			next = node.getNextNode();
//		}
//
//		if (node.prevKey == null) {
//			next = node.getNextNode();
//			next.prevKey = null;
//			next.save();
//			node.nextKey = null;
//			this.storage.delete(node.key);
//			this.head = next;
//			return;
//		} else {
//			prev = node.getPrevNode();
//		}
//
//		next.prevKey = prev.key;
//		prev.nextKey = next.key;
//		node.nextKey = node.prevKey = null;
//		prev.save();
//		next.save();
//		this.storage.delete(node.key);
//	}
//
//	public void linkAfter(Node node) {
//
//	}
//
//	public void linkBefore(Node node) {
//
//	}
//
//	private class Node {
//
//		final K key;
//		final K nextKey;
//		final K prevKey;
//		V value;
//
//		public Node(K prevKey, K nextKey, V value) {
//			this.key = VLinkedList3.this.keyGetter.apply(value);
//			this.nextKey = nextKey;
//			this.prevKey = prevKey;
//			this.value = value;
//		}
//
//		void setKey(K value) {
//			
//		}
//
//		void setValue(V value) {
//
//		}
//
//		K getNextKey() {
//			return this.nextKey;
//		}
//
//		K getPrevKey() {
//			return this.prevKey;
//		}
//
//		Node getNextNode() {
//			if (this.nextKey == null) {
//				return null;
//			}
//			return VLinkedList3.this.load(this.nextKey);
//		}
//
//		Node getPrevNode() {
//			if (this.prevKey == null) {
//				return null;
//			}
//			return VLinkedList3.this.load(this.prevKey);
//		}
//
//		public V getValue() {
//			return this.value;
//		}
//
//		public void save() {
//			VLinkedList3.this.storage.update(this.key, this.value);
//		}
//		
//		K getKey() {
//			return this.key;
//		}
//
//	}
//
//	@Override
//	public String toString() {
//		return "something";
//	}
//
//}
