import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import storages.IStorage;

public class VMap<K, V> implements Map<K, V>, Serializable {

	private final IStorage<K, V> storage;
	private transient Set<Entry<K, V>> entrySet;
	private transient Set<K> keySet;
	private transient Collection<V> valueSet;

	public VMap(IStorage<K, V> storage) {
		this.storage = storage;
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
	public boolean containsKey(Object key) {
		return this.storage.has(key);
	}

	@Override
	public boolean containsValue(Object value) {
		for (Iterator<Entry<K, V>> it = this.storage.iterator(); it.hasNext(); ) {
			Entry<K, V> item = it.next();
			if (item.getValue() == null && value == null) {
				return true;
			}
			if (item.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public V get(Object key) {
		return this.storage.read(key);
	}

	@Override
	public V put(K key, V value) {
		V result = this.storage.read(key);
		this.storage.update(key, value);
		return result;
	}

	@Override
	public V remove(Object key) {
		V result = this.storage.read(key);
		this.storage.delete(key);
		return result;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Iterator<? extends Entry<? extends K, ? extends V>> it = m.entrySet().iterator(); it.hasNext(); ) {
			Entry<? extends K, ? extends V> entry = it.next();
			K key = entry.getKey();
			V value = entry.getValue();
			this.storage.update(key, value);
		}
	}

	@Override
	public void clear() {
		this.storage.clear();
	}

	@Override
	public Set<K> keySet() {
		Set<K> result = keySet;
		if (result == null) {
			result = new VMap<K, V>.KeySet();
			keySet = result;
		}
		return result;
	}

	final class KeySet extends AbstractSet<K> {
		public final int size() {
			return VMap.this.size();
		}

		public final void clear() {
			VMap.this.clear();
		}

		public final Iterator<K> iterator() {
			return new VMap.KeyIterator();
		}

		public final boolean contains(Object o) {
			return containsKey(o);
		}

		public final boolean remove(Object key) {
			return VMap.this.remove(key) != null;
		}

		public final Spliterator<K> spliterator() {
			throw new UnsupportedOperationException("Splititerator is not implemented in this version");
		}

		public final void forEach(Consumer<? super K> action) {
			for (Iterator<Entry<K, V>> it = VMap.this.storage.iterator(); it.hasNext(); ) {
				Entry<K, V> entry = it.next();
				K key = entry.getKey();
				action.accept(key);
			}
		}
	}

	@Override
	public Collection<V> values() {
		Collection<V> result = valueSet;
		if (result == null) {
			result = new VMap.Values();
			valueSet = result;
		}
		return result;
	}

	final class Values extends AbstractCollection<V> {
		public final int size() {
			return VMap.this.size();
		}

		public final void clear() {
			VMap.this.clear();
		}

		public final Iterator<V> iterator() {
			return new VMap.ValueIterator();
		}

		public final boolean contains(Object o) {
			return containsValue(o);
		}

		public final Spliterator<V> spliterator() {
			throw new UnsupportedOperationException("Splititerator is not implemented in this version");
		}

		public final void forEach(Consumer<? super V> action) {
			for (Iterator<Entry<K, V>> it = VMap.this.storage.iterator(); it.hasNext(); ) {
				Entry<K, V> entry = it.next();
				V value = entry.getValue();
				action.accept(value);
			}
		}
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> result = entrySet;
		if (result == null) {
			result = new VMap.EntrySet();
			entrySet = result;
		}
		return result;
	}

	final class EntrySet extends AbstractSet<Entry<K, V>> {
		public final int size() {
			return VMap.this.size();
		}

		public final void clear() {
			VMap.this.clear();
		}

		public final Iterator<Entry<K, V>> iterator() {
			return new VMap.EntryIterator();
		}

		public final boolean contains(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Entry<?, ?> e = (Entry<?, ?>) o;
			Object key = e.getKey();
			if (key == null) {
				return false;
			}
			V item = VMap.this.get(key);
			if (item == null) {
				return e.getValue() == null;
			}
			return item.equals(e.getValue());
		}

		public final boolean remove(Object o) {
			if (o instanceof Map.Entry) {
				Entry<?, ?> e = (Entry<?, ?>) o;
				Object key = e.getKey();
				Object value = e.getValue();
				return VMap.this.remove(key, value);
			}
			return false;
		}

		public final Spliterator<Entry<K, V>> spliterator() {
			throw new UnsupportedOperationException("Splititerator is not implemented in this version");
		}

		public final void forEach(Consumer<? super Entry<K, V>> action) {
			for (Iterator<Entry<K, V>> it = VMap.this.storage.iterator(); it.hasNext(); ) {
				Entry<K, V> entry = it.next();
				action.accept(entry);
			}
		}
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		if (!this.storage.has(key)) {
			return defaultValue;
		}
		return this.storage.read(key);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		for (Iterator<Entry<K, V>> it = VMap.this.storage.iterator(); it.hasNext(); ) {
			Entry<K, V> entry = it.next();
			K key = entry.getKey();
			V value = entry.getValue();
			action.accept(key, value);
		}
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		if (function == null)
			throw new NullPointerException();
		for (Iterator<Entry<K, V>> it = VMap.this.storage.iterator(); it.hasNext(); ) {
			Entry<K, V> entry = it.next();
			K key = entry.getKey();
			V value = entry.getValue();
			V newValue = function.apply(key, value);
			entry.setValue(newValue);
		}
	}

	@Override
	public V putIfAbsent(K key, V value) {
		if (this.storage.has(key)) {
			return this.storage.read(key);
		}
		this.storage.update(key, value);
		return null;
	}

	@Override
	public boolean remove(Object key, Object value) {
		Object curValue = this.storage.read(key);
		if (!Objects.equals(curValue, value) ||
				(curValue == null && !containsKey(key))) {
			return false;
		}
		this.storage.delete(key);
		return true;
	}

	abstract class VMapIterator {
		private Iterator<Entry<K, V>> iterator;

		VMapIterator() {
			this.iterator = VMap.this.storage.iterator();
		}

		public final boolean hasNext() {
			return this.iterator.hasNext();
		}

		public final Entry<K, V> nextEntry() {
			return this.iterator.next();
		}

		public final void remove() {
			this.iterator.remove();
		}

	}

	final class KeyIterator extends VMap<K, V>.VMapIterator
			implements Iterator<K> {
		public final K next() {
			return nextEntry().getKey();
		}
	}

	final class ValueIterator extends VMap<K, V>.VMapIterator
			implements Iterator<V> {
		public final V next() {
			return nextEntry().getValue();
		}
	}

	final class EntryIterator extends VMap<K, V>.VMapIterator
			implements Iterator<Entry<K, V>> {
		public final Entry<K, V> next() {
			return nextEntry();
		}
	}
}
