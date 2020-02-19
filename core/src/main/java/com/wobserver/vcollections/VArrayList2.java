//package com.wobserver.vcollections;
//
//import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;
//import com.wobserver.vcollections.keygenerators.IKeyGenerator;
//import com.wobserver.vcollections.storages.IStorage;
//import com.wobserver.vcollections.storages.sort.ISorter;
//import com.wobserver.vcollections.storages.sort.arrays.Quicksorter;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//import java.util.stream.StreamSupport;
//
//public class VArrayList2<K, V> implements List<V> {
//
//	private final IStorage<K, V> storage;
//
//	public VArrayList2(IStorage<Long, V> storage) {
//		this.storage = storage;
//		if (!(this.storage instanceof IAccessKeyGenerator)) {
//			throw new RuntimeException("Storage must implement ISetKeyGenerator for " + this.getClass().getName());
//		}
//
//		((IAccessKeyGenerator<Long>) this.storage).setKeyGenerator(new IKeyGenerator<Long>() {
//
//			private AtomicReference<Long> nextKeyHolder = new AtomicReference<>(storage.entries());
//
//			@Override
//			public Long get() {
//				return nextKeyHolder.getAndUpdate(v -> v + 1);
//			}
//		});
//	}
//
//	@Override
//	public int size() {
//		if (IStorage.MAX_INTEGER_VALUE < this.storage.entries()) {
//			return -1;
//		}
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
//		for (Iterator<V> it = this.iterator(); it.hasNext(); ) {
//			V value = it.next();
//			if (o == null) {
//				if (value == null) {
//					return true;
//				}
//				continue;
//			}
//			if (o.equals(value)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	@Override
//	public Iterator<V> iterator() {
//		return new IteratorImpl(0);
//	}
//
//	@Override
//	public void forEach(Consumer<? super V> action) {
//		if (action == null) {
//			throw new NullPointerException();
//		}
//		Long index = 0L;
//		for (Iterator<V> it = this.iterator(); it.hasNext(); ++index) {
//			V before = it.next();
//			action.accept(before);
//			V after = it.next();
//			if (before == null) {
//				if (after != null) {
//					this.storage.update(index, after);
//				}
//			} else if (!before.equals(after)) {
//				this.storage.update(index, after);
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
//		int i = 0;
//		for (Iterator<V> it = this.iterator(); it.hasNext(); ) {
//			V value = it.next();
//			// TODO: fix it
////			a[i++] = value;
//		}
//		return null;
//	}
//
//
//	@Override
//	public boolean add(V v) {
//		this.storage.create(v);
//		return true;
//	}
//
//	@Override
//	public boolean remove(Object o) {
//		Long found = -1L;
//		Long index = 0L;
//		Predicate<V> find;
//		if (o == null) {
//			find = v -> v == null;
//		} else {
//			find = v -> o.equals(v);
//		}
//		for (Iterator<V> it = this.iterator(); it.hasNext(); ++index) {
//			V value = it.next();
//			if (find.test(value)) {
//				found = index;
//				break;
//			}
//		}
//		if (found < 0L) {
//			return false;
//		}
//		long end = this.storage.entries() - 1;
//		for (long i = found; i < end; ++i) {
//			this.storage.swap(i, i + 1L);
//		}
//		this.storage.delete(end);
//		return true;
//	}
//
//	@Override
//	public boolean containsAll(Collection<?> c) {
//		return c.stream().allMatch(e -> 0 <= this.indexOf(e));
//	}
//
//	@Override
//	public boolean addAll(Collection<? extends V> c) {
//		c.stream().forEach(this.storage::create);
//		return true;
//	}
//
//	@Override
//	public boolean addAll(int index, Collection<? extends V> c) {
//		long shiftSize = c.size();
//		long i;
//		for (i = this.storage.entries() - 1; index <= i; --i) {
//			V value = this.storage.read(i);
//			this.storage.update(i + shiftSize, value);
//		}
//		// The storage is in an ionconsistent state now, with duplicated values
//		i = index;
//		for (Iterator<? extends V> it = c.iterator(); it.hasNext(); ++i) {
//			V item = it.next();
//			this.storage.update(i, item);
//		}
//		return true;
//	}
//
//	@Override
//	public boolean removeAll(Collection<?> c) {
//		return c.stream().allMatch(this::remove);
//	}
//
//	@Override
//	public boolean removeIf(Predicate<? super V> filter) {
//		if (filter == null) {
//			throw new NullPointerException();
//		}
//		final long originalEnd = this.storage.entries();
//		boolean removed = false;
//		final long end = this.storage.entries();
//		for (long index = 0L; index < end; ++index) {
//			V value = this.storage.read(index);
//			if (filter.test(value)) {
//				this.storage.delete(index);
//				removed = true;
//			}
//		}
//		if (!removed) {
//			return false;
//		}
//		return this.defragment(0L, originalEnd);
//	}
//
//	@Override
//	public boolean retainAll(Collection<?> c) {
//		final long end = this.storage.entries();
//		for (long index = 0; index < end; ++index) {
//			V value = this.storage.read(index);
//			if (!c.contains(value)) {
//				this.storage.delete(index);
//
//			}
//		}
//
//		return this.defragment(0, end);
//	}
//
//
//	private boolean defragment(long start, long originalEnd) {
//		boolean result = false;
//		final long end = this.storage.entries();
//		long next;
//		// Put the start position to an index, which exists.
//		for (; start < end && this.storage.has(start); ++start) ;
//		while (start < end) {
//			V value = null;
//			for (next = start + 1L; next < originalEnd && !this.storage.has(next); ++next) ;
//			value = this.storage.read(next);
//			this.storage.update(start, value);
//			this.storage.delete(next);
//			++start;
//			result = true;
//		}
//		return result;
//	}
//
//	/**
//	 * Make the storage continous.
//	 * This version is costly, because it needs to search the next position
//	 * using a storage iterator reset after every individual defragmentation.
//	 * If we can use, use the defragment(long, long), where the original
//	 * size before the collection fragmented is passed as a parameter
//	 *
//	 * @return true if anything has been changed
//	 */
//	private boolean defragment(long start) {
//		boolean result = false;
//		final long end = this.storage.entries();
//		// Put the start position to an index, which exists.
//		for (; start < end && this.storage.has(start); ++start) ;
//		while (start < end) {
//			Iterator<Map.Entry<Long, V>> it = this.storage.iterator();
//			long next = 0L;
//			V value = null;
//			for (; next <= start && it.hasNext(); ) {
//				Map.Entry<Long, V> entry = it.next();
//				value = entry.getValue();
//				next = entry.getKey();
//			}
//			this.storage.update(start, value);
//			this.storage.delete(next);
//			++start;
//			result = true;
//		}
//		return result;
//	}
//
//	@Override
//	public void sort(Comparator<? super V> c) {
//		ISorter sorter = new Quicksorter<V>(this.storage, c);
//		sorter.run();
//	}
//
//	@Override
//	public void clear() {
//		this.storage.clear();
//	}
//
//	@Override
//	public V get(int index) {
//		if (this.size() <= index) {
//			throw new ArrayIndexOutOfBoundsException();
//		}
//		Long key = Long.valueOf(index);
//		return this.storage.read(key);
//	}
//
//	@Override
//	public V set(int index, V element) {
//		if (this.size() <= index) {
//			throw new ArrayIndexOutOfBoundsException();
//		}
//		Long key = Long.valueOf(index);
//		V removed = this.storage.read(key);
//		this.storage.update(key, element);
//		return removed;
//	}
//
//	@Override
//	public void add(int index, V element) {
//		if (this.size() <= index) {
//			throw new IndexOutOfBoundsException();
//		}
//		Long indexPosition = Long.valueOf(index);
//		if (this.storage.read(indexPosition) == null) {
//			this.storage.update(indexPosition, element);
//			return;
//		}
//
//		Long position = Long.valueOf(this.size() - 1);
//		for (; index <= position; --position) {
//			V value = this.storage.read(position);
//			this.storage.update(position + 1, value);
//		}
//		this.storage.update(position, element);
//	}
//
//	@Override
//	public V remove(int index) {
//		if (this.size() <= index) {
//			throw new IndexOutOfBoundsException();
//		}
//		long end = this.storage.entries() - 1;
//		for (long i = index; i < end; ++i) {
//			this.storage.swap(i, i + 1L);
//		}
//		V result = this.storage.read(end + 1L);
//		this.storage.delete(end);
//		return result;
//	}
//
//	@Override
//	public int indexOf(Object o) {
//		Long index = 0L;
//		Predicate<V> isEqual;
//		if (o == null) {
//			isEqual = v -> v == null;
//		} else {
//			isEqual = v -> o.equals(v);
//		}
//		for (Iterator<V> it = this.iterator(); it.hasNext(); ++index) {
//			V value = it.next();
//			if (isEqual.test(value)) {
//				return index.intValue();
//			}
//		}
//		return -1;
//	}
//
//	@Override
//	public int lastIndexOf(Object o) {
//		Long index = this.storage.entries() - 1;
//		for (ListIterator<V> it = this.listIterator(this.size() - 1); it.hasPrevious(); --index) {
//			V value = it.previous();
//			if (o == null) {
//				if (value == null) {
//					return index.intValue();
//				}
//				continue;
//			}
//			if (o.equals(value)) {
//				return index.intValue();
//			}
//		}
//		return -1;
//	}
//
//	@Override
//	public ListIterator<V> listIterator() {
//		return new ListIteratorImpl(0);
//	}
//
//	@Override
//	public ListIterator<V> listIterator(int index) {
//		return new ListIteratorImpl(index);
//	}
//
//	@Override
//	public List<V> subList(int fromIndex, int toIndex) {
//		if (toIndex <= fromIndex) {
//			throw new ArrayIndexOutOfBoundsException();
//		}
//		int maxSize = toIndex - fromIndex;
//		return this.stream().skip(fromIndex).limit(maxSize).collect(Collectors.toList());
//	}
//
//	@Override
//	public Spliterator<V> spliterator() {
////		throw new NotImplementedException();
//		return null;
//	}
//
//	@Override
//	public Stream<V> stream() {
//		Iterator<V> sourceIterator = this.iterator();
//		return StreamSupport.stream(
//				Spliterators.spliteratorUnknownSize(sourceIterator, Spliterator.ORDERED), false
//		);
//	}
//
//	@Override
//	public Stream<V> parallelStream() {
////		throw new NotImplementedException();
//		return null;
//	}
//
//
//	private class IteratorImpl implements Iterator<V> {
//		protected Long index;
//		protected boolean modified;
//
//		IteratorImpl(int start) {
//			this.index = Long.valueOf(start);
//		}
//
//		IteratorImpl(long start) {
//			this.index = start;
//		}
//
//		public final boolean hasNext() {
//			return 0 <= this.index && this.index < VArrayList2.this.storage.entries();
//		}
//
//		public final V next() {
//			if (VArrayList2.this.storage.entries() <= this.index) {
//				throw new IndexOutOfBoundsException();
//			}
//			V result = VArrayList2.this.storage.read(this.index);
//			this.modified = false;
//			++this.index;
//			return result;
//		}
//
//		public final void remove() {
//			if (VArrayList2.this.storage.entries() <= this.index) {
//				throw new IndexOutOfBoundsException();
//			}
//			if (this.modified || this.index < 1) {
//				throw new IllegalStateException();
//			}
//			VArrayList2.this.storage.delete(this.index);
//			this.modified = true;
//		}
//	}
//
//	private class ListIteratorImpl extends IteratorImpl implements ListIterator<V> {
//
//		ListIteratorImpl(int start) {
//			super(start);
//		}
//
//		@Override
//		public boolean hasPrevious() {
//			return 0 <= this.index && this.index < VArrayList2.this.storage.entries();
//		}
//
//		@Override
//		public V previous() {
//			if (this.index < 0) {
//				throw new IndexOutOfBoundsException();
//			}
//			V result = VArrayList2.this.storage.read(this.index);
//			this.modified = false;
//			--this.index;
//			return result;
//		}
//
//		@Override
//		public int nextIndex() {
//			int result = this.index.intValue() + 1;
//			return result;
//		}
//
//		@Override
//		public int previousIndex() {
//			int result = this.index.intValue() - 1;
//			return result;
//		}
//
//		@Override
//		public void set(V v) {
//			if (this.modified) {
//				throw new IllegalStateException();
//			}
//			VArrayList2.this.storage.update(this.index, v);
//		}
//
//		@Override
//		public void add(V v) {
//			if (this.modified) {
//				throw new IllegalStateException();
//			}
//
//			Long position = VArrayList2.this.storage.entries() - 1;
//			for (; index <= position; --position) {
//				V value = VArrayList2.this.storage.read(position);
//				VArrayList2.this.storage.update(position + 1, value);
//			}
//			VArrayList2.this.storage.update(index, v);
//			this.modified = true;
//		}
//	}
//
//}
