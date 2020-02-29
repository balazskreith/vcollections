package com.wobserver.vcollections;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.junit.jupiter.api.Test;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public interface ListTest<V, T extends List<V>> {

	List<V> makeList(String... items);

	V toItem(String item);

	List<V> asArrayList(String... items);

	String getValue(V value);

	V setValue(V item, String value);

	/***********************************************************************
	 * Scenario: Focuses on projection functionality
	 * toArray, subList, get
	 **********************************************************************/
	/**
	 * <b>Given</b>: A list with a toArray functionality
	 *
	 * <b>When</b>: we invoke toArray
	 *
	 * <b>Then</b>: we check if the toArray has keep the order and list every items
	 */
	@Test
	default void shouldExecuteToArray() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
		Object[] values = list.toArray();

		// Then
		assertEquals(values[0], list.get(0));
		assertEquals(values[1], list.get(1));
		assertEquals(values[2], list.get(2));
	}

	/**
	 * <b>Given</b>: A list with a toArray functionality
	 *
	 * <b>When</b>: we invoke toArray
	 *
	 * <b>Then</b>: we check if the toArray has keep the order and list every items
	 */
	@Test
	default void shouldExecuteSubList() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
		List<V> sublist = list.subList(1, 2);
		Object[] values = sublist.toArray();

		// Then
		assertEquals(values[0], list.get(1));
		assertEquals(1, sublist.size());
	}

	/***********************************************************************
	 * Scenario: Focuses on iterator functionality
	 * forEach, iterator, listIterator, spliterator, stream, parallelStream
	 **********************************************************************/

	/**
	 * <b>Given</b>: a filled array
	 *
	 * <b>When</b>: we call parallelStream
	 *
	 * <b>Then</b>: throws  NotImplementedException
	 */
	@Test
	default void shouldParallelStream() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
//		assertThrows(NotImplementedException.class, () -> {
//			list.parallelStream();
//		});

		// Then
	}

	/**
	 * <b>Given</b>: a filled array
	 *
	 * <b>When</b>: we call splititerate
	 *
	 * <b>Then</b>: throws  NotImplementedException
	 */
	@Test
	default void shouldSplitIterate() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
//		assertThrows(NotImplementedException.class, () -> {
//			list.parallelStream();
//		});

		// Then
	}

	/**
	 * <b>Given</b>: a filled array
	 *
	 * <b>When</b>: we iterate the list backwards
	 *
	 * <b>Then</b>: all items can be reached in the reversed order as we filled.
	 */
	@Test
	default void shouldListiterate() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
		List<V> values = new ArrayList<>();
		for (ListIterator<V> it = list.listIterator(2); it.hasPrevious(); ) {
			values.add(it.previous());
		}

		// Then
		assertEquals(toItem("value"), values.get(2));
		assertEquals(toItem(null), values.get(1));
		assertEquals(toItem("value2"), values.get(0));
	}

	/**
	 * <b>Given</b>: a filled array
	 *
	 * <b>When</b>: we iterate and save the iteration into a list
	 *
	 * <b>Then</b>: all items can be reached in the same order as we filled.
	 */
	@Test
	default void shouldIterate() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
		List<V> values = new ArrayList<>();
		for (Iterator<V> it = list.iterator(); it.hasNext(); ) {
			values.add(it.next());
		}

		// Then
		assertEquals(toItem("value"), values.get(0));
		assertEquals(toItem(null), values.get(1));
		assertEquals(toItem("value2"), values.get(2));
	}

	/**
	 * <b>Given</b>: an array
	 *
	 * <b>When</b>:
	 *
	 * <b>Then</b>:
	 */
	@Test
	default void shouldApplyFoeach() {
		// Given
//		List<String> list = this.makeList("value", null, "value2");
//		List<String> list = Arrays.asList("1", "2", "3");

		// When
//		list.forEach(v -> v);

		// Then
//		assertEquals(0, list.size());
//		assertTrue(list.isEmpty());
	}


	/**
	 * <b>Given</b>: a list filled with ("value', null, "value2")
	 *
	 * <b>When</b>: we clear
	 *
	 * <b>Then</b>: we check if the list is empty
	 * <b>and</b> size is 0
	 */
	@Test
	default void shouldClear() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
		list.clear();

		// Then
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());
	}

	/**
	 * <b>Given</b>: an empty list
	 *
	 * <b>When</b>: we set null
	 *
	 * <b>Then</b>: it throws {@link IndexOutOfBoundsException}
	 */
	@Test
	default void shouldThrowIndexOutOfBoundsException() {
		// Given
		List<V> list = this.makeList();

		// When
		assertThrows(IndexOutOfBoundsException.class, () -> {
			list.set(0, null);
		});
	}

	/**
	 * <b>Given</b>: an empty list
	 *
	 * <b>When</b>: we set null
	 *
	 * <b>Then</b>: we check if the size is 1, and the get(0) is null
	 * <b>and</b> if the size is 1
	 */
	@Test
	default void shouldSetNullValue() {
		// Given
		List<V> list = this.makeList("value");

		// When
		V value = setValue(list.get(0), null);
		list.set(0, value);

		// Then
		V result = list.get(0);
		assertEquals(value, result);
		assertEquals(1, list.size());
	}

	/**
	 * <b>Given</b>: a list filled with ("value', null, "value2")
	 *
	 * <b>When</b>: we retain (null)
	 *
	 * <b>Then</b>: we check if value, value2 removed
	 * <b>and</b> if the size is 1
	 */
	@Test
	default void shouldRetain() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
		List<V> retain = new ArrayList<>();
		retain.add(toItem(null));
		list.retainAll(retain);

		// Then
		assertFalse(asArrayList("value1", "value2").stream().anyMatch(list::contains));
		assertEquals(1, list.size());
	}

	/**
	 * <b>Given</b>: a list filled with numbers from 1 to 10
	 *
	 * <b>When</b>: we remove odds
	 *
	 * <b>Then</b>: we check if evens are retained
	 * <b>and</b> if the size is 5
	 */
	@Test
	default void shouldRemoveOdds() {
		// Given
		List<V> list = this.makeList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

		// When
		list.removeIf(item -> Integer.valueOf(getValue(item)) % 2 == 1);

		// Then
		assertArrayEquals(asArrayList("2", "4", "6", "8", "10").toArray(), list.toArray());
		assertEquals(5, list.size());
	}

	/**
	 * <b>Given</b>: a list filled with ("value', null, "value2")
	 *
	 * <b>When</b>: we removeIf value is null
	 *
	 * <b>Then</b>: we check whether null is removed,
	 * <b>and</b> if the size is 2
	 * <b>and</b> and it is not empty
	 */
	@Test
	default void shouldRemoveNotNullItems() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
		list.removeIf(item -> getValue(item) == null);

		// Then
		assertFalse(list.contains(toItem(null)));
		assertFalse(list.isEmpty());
		assertEquals(2, list.size());
	}

	/**
	 * <b>Given</b>: a list filled with ("value', null, "value2")
	 *
	 * <b>When</b>: we remove null
	 *
	 * <b>Then</b>: we check whether the value is added
	 * <b>and</b> if the size increased
	 * <b>and</b> and it is not empty
	 */
	@Test
	default void shouldRemoveItem() {
		// Given
		List<V> list = this.makeList("value", null, "value2");

		// When
		list.remove(toItem(null));

		// Then
		assertFalse(list.contains(toItem(null)));
		assertFalse(list.isEmpty());
		assertEquals(2, list.size());
	}

	/**
	 * <b>Given</b>: a list filled with ("v1", null, "v2")
	 *
	 * <b>When</b>: we try to remove ("v2", null) by invoking removeAll
	 *
	 * <b>Then</b>: we check whether the values are removed
	 * <b>and</b> size is equal to 1
	 */
	@Test
	default void shouldRemoveAllItems() {
		// Given
		List<V> list = this.makeList("v1", null, "v2");

		// When
		List<V> items = asArrayList("v2", null);
		list.removeAll(items);

		// Then
		assertFalse(items.stream().anyMatch(list::contains));
		assertEquals(1, list.size());
	}

	/**
	 * <b>Given</b>: an empty list
	 *
	 * <b>When</b>: we add a value
	 *
	 * <b>Then</b>: we check whether the value is added
	 * <b>and</b> if the size increased
	 * <b>and</b> and it is not empty
	 */
	@Test
	default void shouldAddItem() {
		// Given
		List<V> list = this.makeList();

		// When
		list.add(toItem("value"));

		// Then
		assertTrue(list.contains(toItem("value")));
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
	}

	/**
	 * <b>Given</b>: an empty list
	 *
	 * <b>When</b>: we add a values by invoking addAll
	 *
	 * <b>Then</b>: we check whether the values are added
	 * <b>and</b> if the size increased
	 * <b>and</b> and it is not empty
	 */
	@Test
	default void shouldAddAllItems() {
		// Given
		List<V> list = this.makeList();

		// When
		List<V> items = asArrayList("v1", "v2", "v3");
		list.addAll(items);

		// Then
		assertTrue(items.stream().allMatch(list::contains));
		assertFalse(list.isEmpty());
		assertEquals(3, list.size());
	}

	/**
	 * <b>Given</b>: a list with values of "v1", and "v5"
	 *
	 * <b>When</b>: we add a values by invoking addAll at position 1
	 *
	 * <b>Then</b>: we check whether the values are added
	 * <b>and</b> if the size increased
	 */
	@Test
	default void shouldAddAllItemsAtPosition() {
		// Given
		List<V> list = this.makeList("v1", "v5");

		// When
		List<V> items = asArrayList("v2", "v3", "v4");
		list.addAll(1, items);

		// Then
		assertArrayEquals(asArrayList("v1", "v2", "v3", "v4", "v5").toArray(), list.toArray());
		assertEquals(5, list.size());
	}

	/**
	 * <b>Scenario</b>:
	 * retrievers:
	 * size, isEmpty, contains, get, containsAll,
	 * indexOf, lastIndexOf,
	 *
	 * <b>Given</b>: a filled storage with element ( "value", null) used in a list
	 *
	 * <b>When</b>: we check contains funtionality
	 *
	 * <b>Then</b>: we read the "value"
	 */
	@Test
	default void shouldGetIndexOfs() {
		// Given
		List<V> list = this.makeList("value", null, "value", null);

		// When

		// Then
		assertEquals(0, list.indexOf(toItem("value")));
		assertEquals(2, list.lastIndexOf(toItem("value")));
		assertEquals(1, list.indexOf(toItem(null)));
		assertEquals(3, list.lastIndexOf(toItem(null)));
		assertEquals(-1, list.indexOf(toItem("non-value")));
		assertEquals(-1, list.lastIndexOf(toItem("non-value")));
	}

	/**
	 * <b>Given</b>: a filled storage with element ( "value", null) used in a list
	 *
	 * <b>When</b>: we check contains funtionality
	 *
	 * <b>Then</b>: we read the "value"
	 */
	@Test
	default void shouldContainsAllValue() {
		// Given
		List<V> list = this.makeList("value", null);

		// When

		// Then
		assertTrue(list.containsAll(asArrayList("value", null)));
		assertFalse(list.containsAll(asArrayList("non-value", null)));
	}


	/**
	 * <b>Given</b>: a filled storage with element ( "value", null) used in a list
	 *
	 * <b>When</b>: we check contains funtionality
	 *
	 * <b>Then</b>: we read the "value"
	 */
	@Test
	default void shouldContainsValue() {
		// Given
		List<V> list = this.makeList("value", null);

		// When

		// Then
		assertTrue(list.contains(toItem("value")));
		assertTrue(list.contains(toItem(null)));
		assertFalse(list.contains(toItem("not-value")));
	}

	/**
	 * <b>Given</b>: a filled storage with element (0, "value") used in a list
	 *
	 * <b>When</b>: we get(0),
	 *
	 * <b>Then</b>: we read "value"
	 */
	@Test
	default void shouldGetValue() {
		// Given
		List<V> list = this.makeList("value");

		// When
		V value = list.get(0);

		// Then
		assertEquals(1, list.size());
		assertEquals(toItem("value"), value);
		assertFalse(list.isEmpty());
	}

	/**
	 * <b>Given</b>: A successfully constructed {@link List}
	 *
	 * <b>When</b>: we check its emptyness
	 *
	 * <b>Then</b>: entries equals to 0
	 * <b>and</b> isEmpty should be true
	 * <b>and</b>
	 */
	@Test
	default void shouldBeEmpty() {
		// Given
		List<V> list = this.makeList();

		// When

		// Then
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());
	}


//	/*************************************************************************
//	 * <b>Scenario</b>: Test focuses on modifying operations, like:
//	 * add, remove, addAll, removeAll, removeIf, retainAll, set, clear, sort
//	 *************************************************************************/
//
//	/**
//	 * <b>Given</b>: a list filled with sorted items
//	 *
//	 * <b>When</b>: We sort it
//	 *
//	 * <b>Then</b>: the array is sorted
//	 */
//	@Test
//	default void shouldSortAlreadySortedArray() {
//		// Given
//		List<Integer> list = new VArrayList(new MemoryStorage(getIndexedMap(1, 2, 3, 4, 5, 6, 7, 8, 9), IStorage.NO_MAX_SIZE));
//
//		// When
//		list.sort(Integer::compareTo);
//
//		// Then
//		assertArrayEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9).toArray(), list.toArray());
//	}
//
//	/**
//	 * <b>Given</b>: a list filled with unsorted elements
//	 *
//	 * <b>When</b>: We sort it
//	 *
//	 * <b>Then</b>: the array is sorted
//	 */
//	@Test
//	@Disabled("Sorter is under review")
//	default void shouldSortUnsortedArray() {
//		// Given
//		List<Integer> list = new VArrayList(new MemoryStorage(getIndexedMap(5, 6, 4, 8, 7, 9, 2, 3, 1), IStorage.NO_MAX_SIZE));
//
//		// When
//		list.sort(Integer::compareTo);
//
//		// Then
//		assertArrayEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9).toArray(), list.toArray());
//	}
//
//	/**
//	 * <b>Given</b>: a list filled with reverse sorted array
//	 *
//	 * <b>When</b>: We sort it
//	 *
//	 * <b>Then</b>: the array is sorted
//	 */
//	@Test
//	@Disabled("Sorter is under review")
//	default void shouldSortReversesortedArray() {
//		// Given
//		List<Integer> list = new VArrayList(new MemoryStorage(getIndexedMap(9, 8, 7, 6, 5, 4, 3, 2, 1), IStorage.NO_MAX_SIZE));
//
//		// When
//		list.sort(Integer::compareTo);
//
//		// Then
//		assertArrayEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9).toArray(), list.toArray());
//	}
//
//	/**
//	 * <b>Given</b>: a list filled with uniform items
//	 *
//	 * <b>When</b>: We sort it
//	 *
//	 * <b>Then</b>: the array is the same
//	 */
//	@Test
//	default void shouldNotSortUniformArray() {
//		// Given
//		List<Integer> list = new VArrayList(new MemoryStorage(getIndexedMap(2, 2, 2, 2, 2, 2), IStorage.NO_MAX_SIZE));
//
//		// When
//		list.sort(Integer::compareTo);
//
//		// Then
//		assertArrayEquals(Arrays.asList(2, 2, 2, 2, 2, 2).toArray(), list.toArray());
//	}
//	default Map<Long, Integer> getIndexedMap(Integer... values) {
//		AtomicReference<Long> indexHolder = new AtomicReference<>(0L);
//		return Arrays.asList(values)
//				.stream()
//				.collect(Collectors.toMap(
//						value -> indexHolder.getAndUpdate(v -> v + 1),
//						value -> value
//				));
//	}
}
