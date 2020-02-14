package com.wobserver.vcollections;

import com.wobserver.vcollections.keygenerators.IKeyGenerator;
import com.wobserver.vcollections.storages.IStorage;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;
import com.wobserver.vcollections.storages.sort.ISorter;
import com.wobserver.vcollections.storages.sort.arrays.Quicksorter;

public class VArrayList<T> implements List<T> {

	private final IStorage<Long, T> storage;

	public VArrayList(IStorage<Long, T> storage) {
		this.storage = storage;
		if (!(this.storage instanceof IAccessKeyGenerator)) {
			throw new RuntimeException("Storage must implement ISetKeyGenerator for " + this.getClass().getName());
		}

		((IAccessKeyGenerator<Long>) this.storage).setKeyGenerator(new IKeyGenerator<Long>() {

			private AtomicReference<Long> nextKeyHolder = new AtomicReference<>(storage.entries());

			@Override
			public Long get() {
				return nextKeyHolder.getAndUpdate(v -> v + 1);
			}
		});
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
		for (Iterator<T> it = this.iterator(); it.hasNext(); ) {
			T value = it.next();
			if (o == null) {
				if (value == null) {
					return true;
				}
				continue;
			}
			if (o.equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		return new IteratorImpl(0);
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		if (action == null) {
			throw new NullPointerException();
		}
		Long index = 0L;
		for (Iterator<T> it = this.iterator(); it.hasNext(); ++index) {
			T before = it.next();
			action.accept(before);
			T after = it.next();
			if (before == null) {
				if (after != null) {
					this.storage.update(index, after);
				}
			} else if (!before.equals(after)) {
				this.storage.update(index, after);
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
		int i = 0;
		for (Iterator<T> it = this.iterator(); it.hasNext(); ) {
			T value = it.next();
			// TODO: fix it
//			a[i++] = value;
		}
		return null;
	}


	@Override
	public boolean add(T t) {
		this.storage.create(t);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		Long found = -1L;
		Long index = 0L;
		for (Iterator<T> it = this.iterator(); it.hasNext(); ++index) {
			T value = it.next();
			if (o == null) {
				if (value == null) {
					found = index;
					break;
				}
				continue;
			}
			if (o.equals(value)) {
				found = index;
				break;
			}
		}
		if (found < 0L) {
			return false;
		}
		long end = this.storage.entries() - 1;
		for (long i = found; i < end; ++i) {
			this.storage.swap(i, i + 1L);
		}
		this.storage.delete(end);
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return c.stream().allMatch(e -> 0 <= this.indexOf(e));
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		c.stream().forEach(this.storage::create);
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		long shiftSize = c.size();
		long i;
		for (i = this.storage.entries() - 1; index <= i; --i) {
			T value = this.storage.read(i);
			this.storage.update(i + shiftSize, value);
		}
		// The storage is in an ionconsistent state now, with duplicated values
		i = index;
		for (Iterator<? extends T> it = c.iterator(); it.hasNext(); ++i) {
			T item = it.next();
			this.storage.update(i, item);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return c.stream().allMatch(this::remove);
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		if (filter == null) {
			throw new NullPointerException();
		}
		final long originalEnd = this.storage.entries();
		boolean removed = false;
		final long end = this.storage.entries();
		for (long index = 0L; index < end; ++index) {
			T value = this.storage.read(index);
			if (filter.test(value)) {
				this.storage.delete(index);
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
			T value = this.storage.read(index);
			if (!c.contains(value)) {
				this.storage.delete(index);

			}
		}

		return this.defragment(0, end);
	}


	private boolean defragment(long start, long originalEnd) {
		boolean result = false;
		final long end = this.storage.entries();
		long next;
		// Put the start position to an index, which exists.
		for (; start < end && this.storage.has(start); ++start) ;
		while (start < end) {
			T value = null;
			for (next = start + 1L; next < originalEnd && !this.storage.has(next); ++next) ;
			value = this.storage.read(next);
			this.storage.update(start, value);
			this.storage.delete(next);
			++start;
			result = true;
		}
		return result;
	}

	/**
	 * Make the storage continous.
	 * This version is costly, because it needs to search the next position
	 * using a storage iterator reset after every individual defragmentation.
	 * If we can use, use the defragment(long, long), where the original
	 * size before the collection fragmented is passed as a parameter
	 *
	 * @return true if anything has been changed
	 */
	private boolean defragment(long start) {
		boolean result = false;
		final long end = this.storage.entries();
		// Put the start position to an index, which exists.
		for (; start < end && this.storage.has(start); ++start) ;
		while (start < end) {
			Iterator<Map.Entry<Long, T>> it = this.storage.iterator();
			long next = 0L;
			T value = null;
			for (; next <= start && it.hasNext(); ) {
				Map.Entry<Long, T> entry = it.next();
				value = entry.getValue();
				next = entry.getKey();
			}
			this.storage.update(start, value);
			this.storage.delete(next);
			++start;
			result = true;
		}
		return result;
	}

	@Override
	public void sort(Comparator<? super T> c) {
		ISorter sorter = new Quicksorter<T>(this.storage, c);
		sorter.run();
	}

	@Override
	public void clear() {
		this.storage.clear();
	}

	@Override
	public T get(int index) {
		if (this.size() <= index) {
			throw new ArrayIndexOutOfBoundsException();
		}
		Long key = Long.valueOf(index);
		return this.storage.read(key);
	}

	@Override
	public T set(int index, T element) {
		if (this.size() <= index) {
			throw new ArrayIndexOutOfBoundsException();
		}
		Long key = Long.valueOf(index);
		T removed = this.storage.read(key);
		this.storage.update(key, element);
		return removed;
	}

	@Override
	public void add(int index, T element) {
		if (this.size() <= index) {
			throw new IndexOutOfBoundsException();
		}
		Long indexPosition = Long.valueOf(index);
		if (this.storage.read(indexPosition) == null) {
			this.storage.update(indexPosition, element);
			return;
		}

		Long position = Long.valueOf(this.size() - 1);
		for (; index <= position; --position) {
			T value = this.storage.read(position);
			this.storage.update(position + 1, value);
		}
		this.storage.update(position, element);
	}

	@Override
	public T remove(int index) {
		if (this.size() <= index) {
			throw new IndexOutOfBoundsException();
		}
		long end = this.storage.entries() - 1;
		for (long i = index; i < end; ++i) {
			this.storage.swap(i, i + 1L);
		}
		T result = this.storage.read(end + 1L);
		this.storage.delete(end);
		return result;
	}

	@Override
	public int indexOf(Object o) {
		Long index = 0L;
		for (Iterator<T> it = this.iterator(); it.hasNext(); ++index) {
			T value = it.next();
			if (o == null) {
				if (value == null) {
					return index.intValue();
				}
				continue;
			}
			if (o.equals(value)) {
				return index.intValue();
			}

		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		Long index = this.storage.entries() - 1;
		for (ListIterator<T> it = this.listIterator(this.size() - 1); it.hasPrevious(); --index) {
			T value = it.previous();
			if (o == null) {
				if (value == null) {
					return index.intValue();
				}
				continue;
			}
			if (o.equals(value)) {
				return index.intValue();
			}
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		return new ListIteratorImpl(0);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new ListIteratorImpl(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		if (toIndex <= fromIndex) {
			throw new ArrayIndexOutOfBoundsException();
		}
		int maxSize = toIndex - fromIndex;
		return this.stream().skip(fromIndex).limit(maxSize).collect(Collectors.toList());
	}

	@Override
	public Spliterator<T> spliterator() {
//		throw new NotImplementedException();
		return null;
	}

	@Override
	public Stream<T> stream() {
		Iterator<T> sourceIterator = this.iterator();
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(sourceIterator, Spliterator.ORDERED), false
		);
	}

	@Override
	public Stream<T> parallelStream() {
//		throw new NotImplementedException();
		return null;
	}


	private class IteratorImpl implements Iterator<T> {
		protected Long index;
		protected boolean modified;

		IteratorImpl(int start) {
			this.index = Long.valueOf(start);
		}

		IteratorImpl(long start) {
			this.index = start;
		}

		public final boolean hasNext() {
			return 0 <= this.index && this.index < VArrayList.this.storage.entries();
		}

		public final T next() {
			if (VArrayList.this.storage.entries() <= this.index) {
				throw new IndexOutOfBoundsException();
			}
			T result = VArrayList.this.storage.read(this.index);
			this.modified = false;
			++this.index;
			return result;
		}

		public final void remove() {
			if (VArrayList.this.storage.entries() <= this.index) {
				throw new IndexOutOfBoundsException();
			}
			if (this.modified || this.index < 1) {
				throw new IllegalStateException();
			}
			VArrayList.this.storage.delete(this.index);
			this.modified = true;
		}
	}

	private class ListIteratorImpl extends IteratorImpl implements ListIterator<T> {

		ListIteratorImpl(int start) {
			super(start);
		}

		@Override
		public boolean hasPrevious() {
			return 0 <= this.index && this.index < VArrayList.this.storage.entries();
		}

		@Override
		public T previous() {
			if (this.index < 0) {
				throw new IndexOutOfBoundsException();
			}
			T result = VArrayList.this.storage.read(this.index);
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
		public void set(T t) {
			if (this.modified) {
				throw new IllegalStateException();
			}
			VArrayList.this.storage.update(this.index, t);
		}

		@Override
		public void add(T t) {
			if (this.modified) {
				throw new IllegalStateException();
			}

			Long position = VArrayList.this.storage.entries() - 1;
			for (; index <= position; --position) {
				T value = VArrayList.this.storage.read(position);
				VArrayList.this.storage.update(position + 1, value);
			}
			VArrayList.this.storage.update(index, t);
			this.modified = true;
		}
	}

}
