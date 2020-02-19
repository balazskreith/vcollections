package com.wobserver.vcollections.storages;

import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bson.Document;

public class MongoStorage<K, V> implements IStorage<K, V> {

	private final long capacity;
	private final Class<V> valueType;
	private final Function<V, K> keyExtractor;
	private final MongoConnection<V> connection;
	private final String keyFieldInDocument;
	private final CapacityChecker<K> capacityChecker;
	private final BiConsumer<V, V> swapper;

	public MongoStorage(MongoConnection<V> connection, Function<V, K> keyExtractor, String keyFieldInDocument, long capacity, Class<V> valueType, BiConsumer<V, V> swapper) {
		this.capacity = capacity;
		this.valueType = valueType;
		this.keyExtractor = keyExtractor;
		this.keyFieldInDocument = keyFieldInDocument;
		this.capacityChecker = new CapacityChecker<>(this, capacity);
		this.connection = connection;
		this.swapper = swapper;
	}


	@Override
	public Long entries() {
		return this.connection.getCollectionSize();
	}

	@Override
	public K create(V value) {
		this.capacityChecker.checkForCreate();
		this.connection.getCollection().insertOne(value);
		return this.keyExtractor.apply(value);
	}

	@Override
	public V read(Object key) {
		FindIterable<V> iterable = this.connection.getCollection().find(eq(this.keyFieldInDocument, key));
		if (iterable == null) {
			return null;
		}
		V result = iterable.first();
		return result;
	}

	@Override
	public void update(K key, V value) {
		this.capacityChecker.checkForUpdate(key);
		this.connection.getCollection().replaceOne(
				Filters.eq(this.keyFieldInDocument, key),
				value,
				new ReplaceOptions().upsert(true).bypassDocumentValidation(true)
		);
	}

	@Override
	public void delete(Object key) {
		this.connection.getCollection().deleteOne(eq(this.keyFieldInDocument, key));
	}

	@Override
	public boolean has(Object key) {
		FindIterable<V> iterable = this.connection.getCollection().find(eq(this.keyFieldInDocument, key));
		if (iterable == null) {
			return false;
		}
		return iterable.first() != null;
	}

	@Override
	public boolean isEmpty() {
		return this.connection.getCollectionSize() == 0;
	}

	@Override
	public boolean isFull() {
		if (this.capacity == IStorage.NO_MAX_SIZE) {
			return false;
		}
		return this.capacity <= this.connection.getCollectionSize();
	}

	@Override
	public void clear() {
		this.connection.getCollection().deleteMany(new Document());
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		final MongoCursor<V> cursor = MongoStorage.this.connection.getCollection().find().iterator();
		return new Iterator<Map.Entry<K, V>>() {

			@Override
			public boolean hasNext() {
				return cursor.hasNext();
			}

			@Override
			public Map.Entry<K, V> next() {
				V value = cursor.next();
				K key = MongoStorage.this.keyExtractor.apply(value);
				return new AbstractMap.SimpleEntry<>(key, value);
			}
		};
	}

	@Override
	public void swap(K key1, K key2) {
		if (this.swapper == null) {
			throw new NullPointerException();
		}
		V value1 = this.read(key1);
		if (value1 == null) {
			throw new KeyNotFoundException(key1.toString());
		}
		V value2 = this.read(key2);
		if (value2 == null) {
			throw new KeyNotFoundException(key2.toString());
		}
		swapper.accept(value1, value2);
		this.update(key1, value1);
		this.update(key2, value2);
	}
}
