package com.wobserver.vcollections;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import com.wobserver.vcollections.storages.IStorage;

public class VLinkedList<T> implements List<T>, Deque<T> {

	private Node head;
	private Node tail;
	private IStorage<UUID, IVLinkedListNode<T>> storage;

	public VLinkedList(IStorage<UUID, IVLinkedListNode<T>> storage) {
		this.storage = storage;
	}

	public VLinkedList(IStorage<UUID, IVLinkedListNode<T>> storage, UUID headUUID, UUID tailUUID) {
		this.storage = storage;
		this.head = this.load(headUUID);
		this.tail = this.load(tailUUID);
	}

	public VLinkedList(IStorage<UUID, IVLinkedListNode<T>> storage, UUID headUUID) {
		this.storage = storage;
		this.head = this.load(headUUID);
		for (this.tail = this.head; this.tail.getNextUUID() != null; this.tail = this.tail.getNextNode()) ;
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
	public Iterator<T> iterator() {
		return new NodeForwardIterator<T>() {
			@Override
			public T next() {
				IVLinkedListNode<T> node = this.nextNode();
				if (node == null) {
					return null;
				}
				return node.getValue();
			}
		};
	}

	@Override
	public Iterator<T> descendingIterator() {
		return new NodeBackwardIterator<T>() {
			@Override
			public T next() {
				IVLinkedListNode<T> node = this.nextNode();
				if (node == null) {
					return null;
				}
				return node.getValue();
			}
		};
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		if (action == null) {
			throw new NullPointerException();
		}
		Node node;
		for (node = this.head; node != null; node = node.getNextNode()) {
			T before = node.getValue();
			action.accept(before);
			T after = node.getValue();
			if (before == null) {
				if (after != null) {
					node.value = after;
					node.save();
				}
			} else if (!before.equals(after)) {
				node.value = after;
				node.save();
			}
		}
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[this.size()];
		int i = 0;
		for (Iterator<T> it = this.iterator(); it.hasNext(); ) {
			T value = it.next();
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
	public void addFirst(T t) {
		Node node;
		if (this.head == null) {
			this.head = this.tail = node = new Node(null, null, t);
			node.save();
			return;
		}
		node = new Node(null, this.head.getUUID(), t);
		this.head.prevUUID = node.getUUID();
		this.head.save();
		this.head = node;
	}

	@Override
	public void addLast(T t) {
		Node node;
		if (this.tail == null) {
			this.head = this.tail = node = new Node(null, null, t);
			node.save();
			return;
		}

		node = new Node(this.tail.getUUID(), null, t);
		node.save();
		this.tail.nextUUID = node.getUUID();
		this.tail.save();
		this.tail = node;
	}

	@Override
	public boolean offerFirst(T t) {
		this.addFirst(t);
		return true;
	}

	@Override
	public boolean offerLast(T t) {
		this.addLast(t);
		return true;
	}

	@Override
	public T removeFirst() {
		if (this.head == null) {
			return null;
		}
		T result = this.head.getValue();
		this.unlinkAndSave(this.head);
		return result;
	}

	@Override
	public T removeLast() {
		if (this.tail == null) {
			return null;
		}
		T result = this.tail.getValue();
		this.unlinkAndSave(this.tail);
		return result;
	}

	@Override
	public T pollFirst() {
		return this.removeFirst();
	}

	@Override
	public T pollLast() {
		return this.removeLast();
	}

	@Override
	public T getFirst() {
		if (this.head == null) {
			return null;
		}
		return this.head.getValue();
	}

	@Override
	public T getLast() {
		if (this.tail == null) {
			return null;
		}
		return this.tail.getValue();
	}

	@Override
	public T peekFirst() {
		return this.getFirst();
	}

	@Override
	public T peekLast() {
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
	public boolean add(T t) {
		this.addLast(t);
		return true;
	}

	@Override
	public boolean offer(T t) {
		this.addLast(t);
		return true;
	}

	@Override
	public T remove() {
		return this.removeFirst();
	}

	@Override
	public T poll() {
		return this.removeFirst();
	}

	@Override
	public T element() {
		return this.getFirst();
	}

	@Override
	public T peek() {
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
	public boolean addAll(Collection<? extends T> c) {
		c.forEach(item -> this.add(item));
		return true;
	}

	@Override
	public void push(T t) {
		this.add(t);
	}

	@Override
	public T pop() {
		return this.removeFirst();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		if (c == null || c.size() < 1) {
			return false;
		}
		if (index < 0 || this.size() <= index) {
			throw new IndexOutOfBoundsException();
		}
		Node after = this.getNode(index);
		Node first = null;
		Node last = null;
		for (T value : c) {
			if (first == null) {
				first = last = new Node(null, null, value);
				continue;
			}
			Node node = new Node(last.uuid, null, value);
			last.nextUUID = node.uuid;
			last.save();
			last = node;
		}

		last.nextUUID = after.uuid;
		last.save();

		if (after == this.head) {
			this.head = first;
		} else {
			Node before = after.getPrevNode();
			before.nextUUID = first.uuid;
			before.save();
			first.prevUUID = before.uuid;
			first.save();
		}

		after.prevUUID = last.uuid;
		after.save();
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
	public boolean removeIf(Predicate<? super T> filter) {
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
			T value = node.getValue();
			if (!filter.test(value)) {
				node = node.getNextNode();
				continue;
			}

			Node next = null;
			if (node.getNextUUID() != null) {
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
	public void replaceAll(UnaryOperator<T> operator) {
		for (Node node = this.head; node != null; node = node.getNextNode()) {
			T value = node.getValue();
			value = operator.apply(value);
			node.value = (value);
			node.save();
		}
	}

	@Override
	public void sort(Comparator<? super T> c) {

	}

	@Override
	public void clear() {
		this.storage.clear();
		this.head = this.tail = null;
	}

	@Override
	public T get(int index) {
		Node node = this.getNode(index);
		if (node == null) {
			return null;
		}
		return node.getValue();
	}

	@Override
	public T set(int index, T element) {
		Node node = this.getNode(index);
		T result = node.getValue();
		node.value = (element);
		node.save();
		return result;
	}

	@Override
	public void add(int index, T element) {
		Node before = this.getNode(index);
		Node node = new Node(before.getUUID(), before.getNextUUID(), element);
		before.nextUUID = (node.getNextUUID());
		before.save();
		node.save();
	}

	@Override
	public T remove(int index) {
		Node node = this.getNode(index);
		T result = node.value;
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
	public ListIterator<T> listIterator() {
		return new NodeForwardListiterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new NodeForwardListiterator(this.getNode(index), index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		List<T> result = new LinkedList<>();
		Node node = this.getNode(fromIndex);
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
	public Spliterator<T> spliterator() {
		return null;
	}

	@Override
	public Stream<T> stream() {
		return null;
	}

	@Override
	public Stream<T> parallelStream() {
		return null;
	}


	private class NodeForwardListiterator implements ListIterator<T> {
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
			return this.actual.getNextUUID() != null;
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
			return this.actual.getPrevUUID() != null;
		}

		public IVLinkedListNode<T> prevNode() {
			if (!this.started) {
				this.started = true;
				return this.actual;
			}
			this.actual = this.actual.getPrevNode();
			--this.position;
			return this.actual;
		}

		@Override
		public T previous() {
			IVLinkedListNode<T> node = this.prevNode();
			if (node == null) {
				return null;
			}
			return node.getValue();
		}

		@Override
		public T next() {
			IVLinkedListNode<T> node = this.nextNode();
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
		public void set(T value) {
			if (this.actual == null) {
				return;
			}
			this.actual.value = (value);
			this.actual.save();
		}

		@Override
		public void add(T value) {
			if (this.actual == null) {
				if (!this.started) { // we have not started it, so this list is empty
					VLinkedList.this.addFirst(value);
				} else { // we stared, and rerached the last
					VLinkedList.this.addLast(value);
				}
				return;
			}
			if (this.actual.getNextUUID() == null) { // last node
				VLinkedList.this.addLast(value);
				return;
			}
			// we have next, and we have prev
			Node next = this.actual.getNextNode();
			Node node = new Node(this.actual.getUUID(), this.actual.getNextUUID(), value);
			this.actual.nextUUID = (node.getUUID());
			next.prevUUID = (node.getUUID());

			node.save();
			this.actual.save();
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
			return this.actual.getNextUUID() != null;
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
			return this.actual.getPrevUUID() != null;
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

	private Node getNode(long position) {
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

	private Node load(UUID uuid) {
		if (this.head != null && this.head.uuid == uuid) {
			return this.head;
		}
		if (this.tail != null && this.tail.uuid == uuid) {
			return this.tail;
		}
		Node result = new Node();
		IVLinkedListNode<T> node = this.storage.read(uuid);
		result.uuid = uuid;
		result.nextUUID = node.getNextUUID();
		result.prevUUID = node.getPrevUUID();
		result.value = node.getValue();
		return result;
	}


	public void unlinkAndSave(Node node) {
		if (node.nextUUID == null && node.prevUUID == null) {
			this.head = VLinkedList.this.tail = null;
			this.storage.delete(node.uuid);
			return;
		}
		Node prev;
		Node next;
		if (node.nextUUID == null) {
			prev = node.getPrevNode();
			prev.nextUUID = null;
			prev.save();
			node.prevUUID = null;
			this.storage.delete(node.uuid);
			this.tail = prev;
			return;
		} else {
			next = node.getNextNode();
		}

		if (node.prevUUID == null) {
			next = node.getNextNode();
			next.prevUUID = null;
			next.save();
			node.nextUUID = null;
			this.storage.delete(node.uuid);
			this.head = next;
			return;
		} else {
			prev = node.getPrevNode();
		}

		next.prevUUID = prev.uuid;
		prev.nextUUID = next.uuid;
		node.nextUUID = node.prevUUID = null;
		prev.save();
		next.save();
		this.storage.delete(node.uuid);
	}

	public void linkAfter(Node node) {

	}

	public void linkBefore(Node node) {

	}

	private class Node implements IVLinkedListNode<T> {
		private UUID uuid;
		private UUID nextUUID;
		private UUID prevUUID;
		private T value;

		private Node() {
		}

		public Node(UUID prevUUID, UUID nextUUID, T value) {
			this.uuid = UUID.randomUUID();
			this.nextUUID = nextUUID;
			this.prevUUID = prevUUID;
			this.value = value;
		}

		public UUID getUUID() {
			return this.uuid;
		}

		public Node getNextNode() {
			if (this.nextUUID == null) {
				return null;
			}
			return VLinkedList.this.load(this.nextUUID);
		}

		public Node getPrevNode() {
			if (this.prevUUID == null) {
				return null;
			}
			return VLinkedList.this.load(this.prevUUID);
		}

		@Override
		public UUID getNextUUID() {
			return this.nextUUID;
		}

		@Override
		public UUID getPrevUUID() {
			return this.prevUUID;
		}

		@Override
		public T getValue() {
			return this.value;
		}

		public void save() {
			VLinkedList.this.storage.update(this.uuid, this);
		}
	}

	@Override
	public String toString() {
		return "something";
	}

}
