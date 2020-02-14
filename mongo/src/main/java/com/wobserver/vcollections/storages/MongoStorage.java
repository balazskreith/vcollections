package com.wobserver.vcollections.storages;

import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import org.bson.Document;

public class MongoStorage<K, V> implements IStorage<K, V> {

	private static <TK, TV> Function<TV, TK> makeKeyExtractor(Class<TV> valueType, String keyFieldName) {
		if (keyFieldName.length() < 1) {
			return null;
		}
		Method[] methods = valueType.getMethods();
		if (methods != null) {
			final String methodName = "get" + Character.toUpperCase(keyFieldName.charAt(0)) + (keyFieldName.length() < 2 ? "" : keyFieldName.substring(1));
			Optional<Method> findMethod = Arrays.asList(methods).stream().filter(method -> method.getName().equals(methodName)).findFirst();
			if (findMethod.isPresent()) {
				final Method keyMethod = findMethod.get();
				return new Function<TV, TK>() {
					@Override
					public TK apply(TV value) {
						try {
							return (TK) keyMethod.invoke(value);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						} catch (InvocationTargetException e) {
							throw new RuntimeException(e);
						}
					}
				};
			}
		}
		Field[] fields = valueType.getFields();
		if (fields == null) {
			return null;
		}
		Optional<Field> findField = Arrays.asList(fields).stream().filter(field -> field.getName().equals(keyFieldName)).findFirst();
		if (findField.isPresent()) {
			final Field keyField = findField.get();
			return new Function<TV, TK>() {
				@Override
				public TK apply(TV value) {
					try {
						return (TK) keyField.get(value);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			};
		}
		return null;
	}

	private final long capacity;
	private final Class<V> valueType;
	private final MongoConnection<V> connection;
	private final String keyFieldName;
	private final Function<V, K> keyExtractor;
	private final CapacityChecker<K> capacityChecker;

	public MongoStorage(MongoClientURI uri, String databaseName, String collectionName, String keyFieldName, long capacity, Class<V> valueType) {
		this.connection = new MongoConnection<>(uri, databaseName, collectionName, valueType);
		this.capacity = capacity;
		this.valueType = valueType;

		this.keyFieldName = keyFieldName;
		this.capacityChecker = new CapacityChecker<>(this, capacity);

		if (keyFieldName == null) { // identity
			this.keyExtractor = obj -> (K) obj;
		} else {
			this.keyExtractor = makeKeyExtractor(valueType, keyFieldName);
		}
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
		FindIterable<V> iterable = this.connection.getCollection().find(Filters.eq(this.keyFieldName, key));
		if (iterable == null) {
			return null;
		}
		return iterable.first();
	}

	@Override
	public void update(K key, V value) {
		if (this.has(key)) {
			this.connection.getCollection().replaceOne(Filters.eq(key), value);
		} else {
			this.capacityChecker.checkForCreate();
			this.connection.getCollection().insertOne(value);
		}
	}

	@Override
	public void delete(Object key) {
		this.connection.getCollection().deleteOne(Filters.eq(this.keyFieldName, key));
	}

	@Override
	public boolean has(Object key) {
		return false;
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
		return this.connection.getCollectionSize() <= this.capacity;
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

}
