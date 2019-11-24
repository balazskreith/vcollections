import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public interface DequeTest<T extends Deque<String>> {

	Deque<String> makeDeque(String... items);

	/**
	 * <b>Given</b>: an empty list
	 *
	 * <b>When</b> a value is offered
	 *
	 * <b>Then</b> a peek retrieve the offered value
	 */
	@Test
	default void shouldPeek() {
		// Given
		Deque<String> deque = makeDeque();

		// When
		deque.offer("value");

		// Then
		assertEquals("value", deque.peek());
	}

	/**
	 * <b>Given</b>: an empty deque
	 *
	 * <b>When</b> we add a value
	 *
	 * <b>Then</b> We remove it and its equal and the queue is empty
	 */
	@Test
	default void shouldAddAll() {
		// Given
		Deque<String> deque = makeDeque();

		// When
		List<String> values = Arrays.asList("v1", "v2", "v3");
		deque.addAll(values);

		// Then
		assertArrayEquals(values.toArray(), deque.toArray());
		assertEquals(3, deque.size());
	}

	/**
	 * <b>Given</b>: an empty deque
	 *
	 * <b>When</b> we add a value
	 *
	 * <b>Then</b> We remove it and its equal and the queue is empty
	 */
	@Test
	default void shouldAdd() {
		// Given
		Deque<String> deque = makeDeque();

		// When
		deque.add("value");

		// Then
		assertEquals("value", deque.element());
		assertEquals("value", deque.remove());
	}

	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we offer a value into the beginning of the list
	 *
	 * <b>Then</b> We expect to retrieve it at the beginning
	 */
	@Test
	default void shouldRemoveFirst() {
		// Given
		Deque<String> deque = makeDeque("v1", "v2", "v1");

		// When
		deque.removeFirst();

		// Then
		assertEquals("v2", deque.getFirst());
	}

	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we offer a value into the beginning of the list
	 *
	 * <b>Then</b> We expect to retrieve it at the beginning
	 */
	@Test
	default void shouldRemoveLast() {
		// Given
		Deque<String> deque = makeDeque("v1", "v2", "v3");

		// When
		deque.removeLast();

		// Then
		assertEquals("v2", deque.getLast());
	}

	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we offer a value into the beginning of the list
	 *
	 * <b>Then</b> We expect to retrieve it at the beginning
	 */
	@Test
	default void shouldRemoveFirstOccurrence() {
		// Given
		Deque<String> deque = makeDeque("v1", "v2", "v1");

		// When
		deque.removeFirstOccurrence("v1");

		// Then
		assertEquals("v2", deque.getFirst());
	}

	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we offer a value into the beginning of the list
	 *
	 * <b>Then</b> We expect to retrieve it at the beginning
	 */
	@Test
	default void shouldRemoveLastOccurrence() {
		// Given
		Deque<String> deque = makeDeque("v1", "v2", "v1");

		// When
		deque.removeLastOccurrence("v1");

		// Then
		assertEquals("v1", deque.getFirst());
	}

	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we push a value into the beginning of the list
	 *
	 * <b>Then</b> We expect to retrieve it in order
	 */
	@Test
	default void shouldBePushed() {
		// Given
		Deque<String> deque = makeDeque("v2", "v3");

		// When
		deque.push("v3");

		// Then
		assertEquals("v1", deque.pop());
		assertEquals("v2", deque.pop());
		assertEquals("v3", deque.pop());

	}

	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we push a value into the end of the list
	 *
	 * <b>Then</b> We expect to retrieve it in order
	 */
	@Test
	default void shouldBePolled() {
		// Given
		Deque<String> deque = makeDeque("v1", "v2");

		// When
		deque.push("v3");

		// Then
		assertEquals("v1", deque.pollFirst());
		assertEquals("v3", deque.pollLast());
		assertEquals("v2", deque.poll());
	}


	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we offer a value into the end of the list
	 *
	 * <b>Then</b> We expect to retrieve it at the end
	 */
	@Test
	default void shouldBeOfferedLast() {
		// Given
		Deque<String> deque = makeDeque("v1", "v2");

		// When
		deque.offerLast("v3");

		// Then
		assertEquals("v3", deque.peekLast());
	}

	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we offer a value into the beginning of the list
	 *
	 * <b>Then</b> We expect to retrieve it at the beginning
	 */
	@Test
	default void shouldBeOfferedFirst() {
		// Given
		Deque<String> deque = makeDeque("v2", "v3");

		// When
		deque.offerFirst("v1");

		// Then
		assertEquals("v1", deque.peekFirst());
	}

	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we add a value into the end of the list
	 *
	 * <b>Then</b> We expect to retrieve it at the end
	 */
	@Test
	default void shouldBeAddedLast() {
		// Given
		Deque<String> deque = makeDeque("v1", "v2");

		// When
		deque.addLast("v3");

		// Then
		assertEquals("v3", deque.getLast());
	}

	/**
	 * <b>Given</b>: a filled dequeue
	 *
	 * <b>When</b> we add a value into the beginning of the list
	 *
	 * <b>Then</b> We expect to retrieve it at the beginning
	 */
	@Test
	default void shouldBeAddedFirst() {
		// Given
		Deque<String> deque = makeDeque("v2", "v3");

		// When
		deque.addFirst("v1");

		// Then
		assertEquals("v1", deque.getFirst());
	}
}
