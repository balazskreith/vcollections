//package com.wobserver.vcollections;
//
//import com.wobserver.vcollections.storages.IStorage;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.*;
//import java.util.stream.Stream;
//
//public class VLinkedList2<K, V> implements List<V>, Deque<V> {
//
//	private V head;
//	private V tail;
//	private IStorage<K, V> storage;
//	private final Function<V, K> keyGetter;
//	private final Function<V, K> nextGetter;
//	private final Function<V, K> prevGetter;
//	private final BiConsumer<V, K> nextSetter;
//	private final BiConsumer<V, K> prevSetter;
//
//
//	public VLinkedList2(IStorage<K, V> storage, V head, V tail, Class<V> valueType, String prevFieldName, String nextFieldName) {
//		this.storage = storage;
//	}
//
//	private V nextNode(V actual) {
//		K nextKey = this.nextGetter.apply(actual);
//		if (nextKey == null) {
//			return null;
//		}
//		V result = this.storage.read(nextKey);
//		return result;
//	}
//
//	private V prevNode(V actual) {
//		K prevKey = this.prevGetter.apply(actual);
//		if (prevKey == null) {
//			return null;
//		}
//		V result = this.storage.read(prevKey);
//		return result;
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
//		V node = this.findFirst(o, null);
//		return node != null;
//	}
//
//	@Override
//	public Iterator<V> iterator() {
//		return new NodeForwardIterator<V>() {
//			@Override
//			public V next() {
//				V node = this.nextNode();
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
//				IVLinkedListNode<V> node = this.nextNode();
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
//		V node;
//		for (node = this.head; node != null; node = this.storage.read(this.nextGetter.apply(node))) {
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
//		node = new Node(null, this.head.getUUID(), v);
//		this.head.prevUUID = node.getUUID();
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
//		node = new Node(this.tail.getUUID(), null, v);
//		node.save();
//		this.tail.nextUUID = node.getUUID();
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
//		V result = this.head;
//		this.unlinkAndSave(this.head);
//		return result;
//	}
//
//	@Override
//	public V removeLast() {
//		if (this.tail == null) {
//			return null;
//		}
//		V result = this.tail;
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
//			Node node = new Node(last.uuid, null, value);
//			last.nextUUID = node.uuid;
//			last.save();
//			last = node;
//		}
//
//		last.nextUUID = after.uuid;
//		last.save();
//
//		if (after == this.head) {
//			this.head = first;
//		} else {
//			Node before = after.getPrevNode();
//			before.nextUUID = first.uuid;
//			before.save();
//			first.prevUUID = before.uuid;
//			first.save();
//		}
//
//		after.prevUUID = last.uuid;
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
//			if (node.getNextUUID() != null) {
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
//		Node node = new Node(before.getUUID(), before.getNextUUID(), element);
//		before.nextUUID = (node.getNextUUID());
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
//			this.actual = VLinkedList2.this.head;
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
//			return this.actual.getNextUUID() != null;
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
//			return this.actual.getPrevUUID() != null;
//		}
//
//		public IVLinkedListNode<V> prevNode() {
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
//			IVLinkedListNode<V> node = this.prevNode();
//			if (node == null) {
//				return null;
//			}
//			return node.getValue();
//		}
//
//		@Override
//		public V next() {
//			IVLinkedListNode<V> node = this.nextNode();
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
//				return VLinkedList2.this.size() - 1;
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
//					VLinkedList2.this.addFirst(value);
//				} else { // we stared, and rerached the last
//					VLinkedList2.this.addLast(value);
//				}
//				return;
//			}
//			if (this.actual.getNextUUID() == null) { // last node
//				VLinkedList2.this.addLast(value);
//				return;
//			}
//			// we have next, and we have prev
//			Node next = this.actual.getNextNode();
//			Node node = new Node(this.actual.getUUID(), this.actual.getNextUUID(), value);
//			this.actual.nextUUID = (node.getUUID());
//			next.prevUUID = (node.getUUID());
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
//			this.actual = VLinkedList2.this.head;
//		}
//
//		@Override
//		public boolean hasNext() {
//			if (this.actual == null) {
//				return false;
//			}
//			return this.actual.getNextUUID() != null;
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
//			return this.actual.getPrevUUID() != null;
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
//	private V findFirst(Object o) {
//		return this.findFirst(o, null);
//	}
//
//	private V findFirst(Object o, AtomicReference<Long> positionHolder) {
//		if (this.head == null) {
//			return null;
//		}
//		long position = 0L;
//		V result = null;
//		Function<V, Boolean> tester = this.getTesterFor(o);
//		for (V node = this.head; node != null; node = this.nextNode(node), ++position) {
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
//	private V findLast(Object o) {
//		return findLast(o, null);
//	}
//
//	private V findLast(Object o, AtomicReference<Long> positionHolder) {
//		if (this.tail == null) {
//			return null;
//		}
//		long position = this.storage.entries() - 1L;
//		V result = null;
//		Function<V, Boolean> tester = this.getTesterFor(o);
//		for (V node = this.tail; node != null; node = this.prevNode(node), --position) {
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
//	public void unlinkAndSave(V node) {
//		if (this.nextGetter.apply(node) == null && this.prevGetter.apply(node) == null) {
//			this.head = VLinkedList2.this.tail = null;
//			K key = this.keyGetter.apply(node);
//			this.storage.delete(key);
//			return;
//		}
//		V prev;
//		V next;
//		if (this.nextGetter.apply(node) == null) {
//			prev = this.prevNode(node);
//			this.nextSetter.accept(prev, null);
//
//			prev.save();
//			node.prevUUID = null;
//			this.storage.delete(node.uuid);
//			this.tail = prev;
//			return;
//		} else {
//			next = node.getNextNode();
//		}
//
//		if (node.prevUUID == null) {
//			next = node.getNextNode();
//			next.prevUUID = null;
//			next.save();
//			node.nextUUID = null;
//			this.storage.delete(node.uuid);
//			this.head = next;
//			return;
//		} else {
//			prev = node.getPrevNode();
//		}
//
//		next.prevUUID = prev.uuid;
//		prev.nextUUID = next.uuid;
//		node.nextUUID = node.prevUUID = null;
//		prev.save();
//		next.save();
//		this.storage.delete(node.uuid);
//	}
//
//	@Override
//	public String toString() {
//		return "something";
//	}
//
//}
