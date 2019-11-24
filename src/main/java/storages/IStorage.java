package storages;

import java.util.Iterator;
import java.util.Map;

/**
 * This interface is based on the CRUD schema (Create, Read, Update Delete), where every
 * operation has two different type of invocation mode: a single one, and a batched one.
 * <p>
 * Every storage implementing this interface are able to be used for any virtual collections.
 *
 * @param <K> The type of the key
 * @param <V> the type of the value
 */
public interface IStorage<K, V> {

	/**
	 * Built in contant to use to compare long values to integers
	 */
	long MAX_INTEGER_VALUE = Integer.MAX_VALUE;
	/**
	 * Special value for capacities indicating that it has no limitation
	 */
	long NO_MAX_SIZE = -1;

	/**
	 * The number of entries the storage has
	 *
	 * @return
	 */
	Long entries();

	/**
	 * The number of entries the storage is able to store.
	 * <p>
	 * By default it is {@code IStorage#NO_MAX_SIZE}
	 *
	 * @return
	 */
	default Long capacity() {
		return IStorage.NO_MAX_SIZE;
	}

	/**
	 * Creates an entry in the storage for a given value.
	 * <p>
	 * The underlying implementation must ensure the generation of a unique key
	 * to every inserted value.
	 * <p>
	 * The underlying implementation must check the available capacity for the operation,
	 * and throws exception if there is not enough space to perform the operation.
	 *
	 * @param value the value we want to insert
	 * @return the generated key the value is accessible to.
	 * @throws KeyUniquenessViolationException if the key generated for the value is not unique in the storage
	 * @throws OutOfSpaceException             if the storage is full
	 */
	K create(V value);


	/**
	 * Read a value from the storage belongs to a given key.
	 * <p>
	 * As the key is an Object it depends on the underlying implementation
	 * of how it recognizes it as a key type.
	 *
	 * @param key
	 * @return the corresponding value or null if it does not found a value.
	 * NOTE: If the corresponding value to the key is null, this function returns null.
	 * if you want to know if the key exists or not use the {@link IStorage#has(Object)} function
	 */
	V read(Object key);

	/**
	 * Updates the storage for the given key,value pair.
	 * If the key has not been existed, this method creates the pair, if it has been existed,
	 * this method updates the value for the key.
	 *
	 * @param key
	 * @param value
	 */
	void update(K key, V value);


	/**
	 * Deletes the value from the storage belongs to a key.
	 *
	 * @param key
	 */
	void delete(Object key);

	boolean has(Object key);

	boolean isEmpty();

	boolean isFull();

	void clear();

	/**
	 * @param key1
	 * @param key2
	 * @throws KeyNotFoundException if any of the key has not been found
	 */
	default void swap(K key1, K key2) {
		V value1 = read(key1);
		V value2 = read(key2);
		update(key1, value2);
		update(key2, value1);
	}

	Iterator<Map.Entry<K, V>> iterator();

}
