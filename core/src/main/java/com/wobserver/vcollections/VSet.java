package com.wobserver.vcollections;

import com.wobserver.vcollections.storages.IStorage;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VSet<T> implements Set<T> {

	static private final Integer PRESENT = 1;
	private IStorage<T, Integer> storage;

	@Override
	public int size() {
		if (IStorage.MAX_INTEGER_VALUE < this.storage.entries()) {
			return -1;
		}
		return this.storage.entries().intValue();
	}

	@Override
	public boolean isEmpty() {
		return this.storage.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.storage.has(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			private Iterator<Map.Entry<T, Integer>> iterator = storage.iterator();

			@Override
			public boolean hasNext() {
				return this.iterator.hasNext();
			}

			@Override
			public T next() {
				return this.iterator.next().getKey();
			}
		};
	}

	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return null;
	}

	@Override
	public boolean add(T t) {
		this.storage.update(t, PRESENT);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		this.storage.delete(o);
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return c.stream().allMatch(this.storage::has);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return c.stream().allMatch(this::add);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return c.stream().allMatch(this::remove);
	}

	@Override
	public void clear() {
		this.storage.clear();
	}
}
