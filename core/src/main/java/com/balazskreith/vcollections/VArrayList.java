package com.balazskreith.vcollections;

import com.balazskreith.vcollections.activeconfigs.ArrayListActiveConfig;
import com.balazskreith.vcollections.storages.IStorage;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class VArrayList<K, V> implements List<V> {

	private final ArrayListActiveConfig<K, V> config;
	private final IStorage<K, V> storage;

	public VArrayList(ArrayListActiveConfig<K, V> config) {
		this.config = config;
		this.storage = this.config.storageBuilder.build();
	}

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
		Predicate<V> found = o == null ? v -> v == null : v -> o.equals(v);
		for (Iterator<V> it = this.iterator(); it.hasNext(); ) {
			V value = it.next();
			if (found.test(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<V> iterator() {
		return new IteratorImpl(0);
	}

	@Override
	public void forEach(Consumer<? super V> action) {
		if (action == null) {
			throw new NullPointerException();
		}
		Long index = 0L;
		for (Iterator<V> it = this.iterator(); it.hasNext(); ++index) {
			V before = it.next();
			V after = before;
			action.accept(after);
			if (before != null) {
				if (!before.equals(after)) {
					this.storage.update(this.getKeyFor(index), after);
				}
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
		int i = 0;
		for (Iterator<V> it = this.iterator(); it.hasNext(); ) {
			V value = it.next();
			// TODO: fix it
//			a[i++] = value;
		}
		return null;
	}


	@Override
	public boolean add(V v) {
		this.storage.create(v);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		Long found = -1L;
		Long index = 0L;
		Predicate<V> find;
		if (o == null) {
			find = v -> v == null;
		} else {
			find = v -> o.equals(v);
		}
		for (Iterator<V> it = this.iterator(); it.hasNext(); ++index) {
			V value = it.next();
			if (find.test(value)) {
				found = index;
				break;
			}
		}
		if (found < 0L) {
			return false;
		}
		long end = this.storage.entries() - 1;
		for (long i = found; i < end; ++i) {
			this.storage.swap(this.getKeyFor(i), this.getKeyFor(i + 1L));
		}
		this.storage.delete(this.getKeyFor(end));
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return c.stream().allMatch(e -> 0 <= this.indexOf(e));
	}

	@Override
	public boolean addAll(Collection<? extends V> c) {
		c.stream().forEach(this.storage::create);
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends V> c) {
		long shiftSize = c.size();
		long i;
		for (i = this.storage.entries() - 1; index <= i; --i) {
			K key = this.getKeyFor(i);
			V value = this.storage.read(key);
			this.storage.update(this.getKeyFor(i + shiftSize), value);
		}
		// The storage is in an ionconsistent state now, with duplicated values
		i = index;
		for (Iterator<? extends V> it = c.iterator(); it.hasNext(); ++i) {
			V item = it.next();
			this.storage.update(this.getKeyFor(i), item);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return c.stream().allMatch(this::remove);
	}

	@Override
	public boolean removeIf(Predicate<? super V> filter) {
		if (filter == null) {
			throw new NullPointerException();
		}
		final long originalEnd = this.storage.entries();
		boolean removed = false;
		final long end = this.storage.entries();
		for (long index = 0L; index < end; ++index) {
			V value = this.storage.read(this.getKeyFor(index));
			if (filter.test(value)) {
				this.storage.delete(this.getKeyFor(index));
				removed = true;
			}
		}
		if (!removed) {
			return false;
		}
		return this.defragment(0L, originalEnd);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		final long end = this.storage.entries();
		for (long index = 0; index < end; ++index) {
			V value = this.storage.read(this.getKeyFor(index));
			if (!c.contains(value)) {
				this.storage.delete(this.getKeyFor(index));

			}
		}

		return this.defragment(0, end);
	}


	private boolean defragment(long start, long originalEnd) {
		boolean result = false;
		final long end = this.storage.entries();
		long next;
		// Put the start position to an index, which exists.
		for (; start < end && this.storage.has(this.getKeyFor(start)); ++start) ;
		while (start < end) {
			K startKey = this.getKeyFor(start);
			V value = null;
			for (next = start + 1L; next < originalEnd && !this.storage.has(this.getKeyFor(next)); ++next) ;
			K nextKey = this.getKeyFor(next);
			value = this.storage.read(nextKey);
			this.storage.update(startKey, value);
			this.storage.delete(nextKey);
			++start;
			result = true;
		}
		return result;
	}

	@Override
	public void sort(Comparator<? super V> c) {
//		ISorter sorter = new Quicksorter<T>(this.storage, c);
//		sorter.run();
	}

	@Override
	public void clear() {
		this.storage.clear();
	}

	@Override
	public V get(int index) {
		if (this.size() <= index) {
			throw new ArrayIndexOutOfBoundsException();
		}
		K key = this.getKeyFor(index);
		return this.storage.read(key);
	}

	@Override
	public V set(int index, V element) {
		if (this.size() <= index) {
			throw new ArrayIndexOutOfBoundsException();
		}
		K key = this.getKeyFor(index);
		V removed = this.storage.read(key);
		this.storage.update(key, element);
		return removed;
	}

	@Override
	public void add(int index, V element) {
		if (this.size() <= index) {
			throw new IndexOutOfBoundsException();
		}
		K indexPosition = this.getKeyFor(index);
		if (this.storage.read(indexPosition) == null) {
			this.storage.update(indexPosition, element);
			return;
		}

		Long position = Long.valueOf(this.size() - 1);
		for (; index <= position; --position) {
			K positionKey = this.getKeyFor(position);
			V value = this.storage.read(positionKey);
			this.storage.update(this.getKeyFor(position + 1), value);
		}
		this.storage.update(this.getKeyFor(position), element);
	}

	@Override
	public V remove(int index) {
		if (this.size() <= index) {
			throw new IndexOutOfBoundsException();
		}
		long end = this.storage.entries() - 1;
		for (long i = index; i < end; ++i) {
			this.storage.swap(this.getKeyFor(i), this.getKeyFor(i + 1L));
		}
		V result = this.storage.read(this.getKeyFor(end + 1L));
		this.storage.delete(this.getKeyFor(end));

		return result;
	}

	@Override
	public int indexOf(Object o) {
		Long index = 0L;
		Predicate<V> isEqual;
		if (o == null) {
			isEqual = v -> v == null;
		} else {
			isEqual = v -> o.equals(v);
		}
		for (index = 0L; index < this.size(); ++index) {
			K key = this.getKeyFor(index);
			V value = this.storage.read(key);
			if (isEqual.test(value)) {
				return index.intValue();
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		Long index = this.storage.entries() - 1;
		Predicate<V> found = o == null ? v -> v == null : v -> o.equals(v);
		for (ListIterator<V> it = this.listIterator(this.size() - 1); it.hasPrevious(); --index) {
			V value = it.previous();
			if (found.test(value)) {
				return index.intValue();
			}
		}
		return -1;
	}

	@Override
	public ListIterator<V> listIterator() {
		return new ListIteratorImpl(0);
	}

	@Override
	public ListIterator<V> listIterator(int index) {
		return new ListIteratorImpl(index);
	}

	@Override
	public List<V> subList(int fromIndex, int toIndex) {
		if (toIndex <= fromIndex) {
			throw new ArrayIndexOutOfBoundsException();
		}
		int maxSize = toIndex - fromIndex;
		return this.stream().skip(fromIndex).limit(maxSize).collect(Collectors.toList());
	}

	@Override
	public Spliterator<V> spliterator() {
//		throw new NotImplementedException();
		return null;
	}

	@Override
	public Stream<V> stream() {
		Iterator<V> sourceIterator = this.iterator();
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(sourceIterator, Spliterator.ORDERED), false
		);
	}

	@Override
	public Stream<V> parallelStream() {
//		throw new NotImplementedException();
		return null;
	}

	private class IteratorImpl implements Iterator<V> {
		protected Long index;
		protected boolean modified;
		protected long end;

		IteratorImpl(int start) {
			this.index = Long.valueOf(start);
			this.end = VArrayList.this.storage.entries();
		}

		public final boolean hasNext() {
			return this.index < this.end;
		}

		public final V next() {
			if (VArrayList.this.storage.entries() <= this.index) {
				throw new IndexOutOfBoundsException();
			}
			K key = VArrayList.this.getKeyFor(this.index);
			V result = VArrayList.this.storage.read(key);
			this.modified = false;
			++this.index;
			return result;
		}

		public final void remove() {
			VArrayList.this.remove(this.index);
			--this.end;
			this.modified = false;
		}
	}

	private class ListIteratorImpl extends IteratorImpl implements ListIterator<V> {

		private V prev;

		ListIteratorImpl(int start) {
			super(start);
		}

		@Override
		public boolean hasPrevious() {
			return 0 <= this.index && this.index < VArrayList.this.storage.entries();
		}

		@Override
		public V previous() {
			if (this.index < 0) {
				throw new IndexOutOfBoundsException();
			}
			K key = VArrayList.this.getKeyFor(this.index);
			V result = VArrayList.this.storage.read(key);
			this.modified = false;
			--this.index;
			return result;
		}

		@Override
		public int nextIndex() {
			int result = this.index.intValue() + 1;
			return result;
		}

		@Override
		public int previousIndex() {
			int result = this.index.intValue() - 1;
			return result;
		}

		@Override
		public void set(V v) {
			if (this.modified) {
				throw new IllegalStateException();
			}
			K key = VArrayList.this.getKeyFor(this.index);
			VArrayList.this.storage.update(key, v);
		}

		@Override
		public void add(V v) {
			if (this.modified) {
				throw new IllegalStateException();
			}

			Long position = VArrayList.this.storage.entries() - 1;
			for (; index <= position; --position) {
				K positionKey = VArrayList.this.getKeyFor(position);
				V value = VArrayList.this.storage.read(positionKey);
				K nextPosition = VArrayList.this.getKeyFor(position + 1);
				VArrayList.this.storage.update(nextPosition, value);
			}
			K key = VArrayList.this.getKeyFor(this.index);
			VArrayList.this.storage.update(key, v);
			this.modified = true;
		}
	}


	private K getKeyFor(int index) {

		return this.config.keyAdapter.deconvert((long) index);
	}

	private K getKeyFor(long index) {
		return this.config.keyAdapter.deconvert(index);
	}

	private Long getIndexFor(K key) {
		return this.config.keyAdapter.convert(key);
	}

}
