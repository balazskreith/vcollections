package com.wobserver.vcollections.keygenerators;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class IntegerGeneratorTest {

	private IKeyGenerator<Integer> createGenerator(int minSize, int maxSize, Predicate<Integer> tester) {
		IntegerGenerator result = new IntegerGenerator(minSize, maxSize);
		result.setMaxRetry(500);
		result.setup(tester);
		return result;
	}
	// TODO: one test for the nullpointcheck

	/**
	 * <b>Given</b>: Givan a strict configuration for generating keys in between 0, and 10
	 *
	 * <b>When</b>: We generate keys
	 *
	 * <b>Then</b>: we make sure that no duplicated keys are generated
	 * <b>and</b> no keys generated out of the bounds
	 */
	@Test
	public void shouldStayInRange() {
		// Given
		Map<Integer, Integer> generated = new HashMap<>();
		IKeyGenerator<Integer> generator = createGenerator(0, 10, generated::containsKey);

		// When
		for (int i = 0; i < 10; ++i) {
			Integer key = generator.get();
			generated.put(key, generated.getOrDefault(key, 0) + 1);
		}

		// Then
		assertFalse(generated.values().stream().anyMatch(v -> 1 < v));
		assertFalse(generated.keySet().stream().anyMatch(k -> 10 < k));
	}

	/**
	 * <b>Given</b>: Givan a strict configuration for generating keys in between 0, and 10
	 *
	 * <b>When</b>: We generate keys, but we want more than the range
	 *
	 * <b>Then</b>: We got an exception
	 */
	@Test
	public void shouldThrowsException() {
		// Given
		Map<Integer, Integer> generated = new HashMap<>();
		IKeyGenerator<Integer> generator = createGenerator(0, 10, generated::containsKey);

		// When
		NoKeyGeneratedException expected = null;
		try {
			for (int i = 0; i < 11; ++i) {
				Integer key = generator.get();
				generated.put(key, generated.getOrDefault(key, 0) + 1);
			}
		} catch(NoKeyGeneratedException e) {
			expected = e;
		}

		// Then
		assertNotNull(expected);
	}

	/**
	 * <b>Given</b>:
	 *
	 * <b>When</b>:
	 *
	 * <b>Then</b>:
	 */
}