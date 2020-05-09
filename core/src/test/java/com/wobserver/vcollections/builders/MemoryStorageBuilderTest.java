package com.wobserver.vcollections.builders;

import static org.junit.jupiter.api.Assertions.*;
import com.wobserver.vcollections.storages.IStorage;
import java.io.File;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

class MemoryStorageBuilderTest extends AbstractBuilderTester {

	private static final String RESOURCE_FILE_NAME = "memorybuilder_test_source.yaml";
	private static final String DEFAULT_PROFILE_KEY = "defaultProfile";
	private static final String ILLEGAL_CAPACITY_PROFILE_KEY = "illegalCapacityProfile";
	private static final String VALID_CAPACITY_PROFILE_KEY = "validCapacityProfile";
	private static final String NOT_VALID_PROPERTY_PROFILE_1_KEY = "notValidPropertyProfile1";
	private static final String NOT_VALID_PROPERTY_PROFILE_2_KEY = "notValidPropertyProfile2";
	private static final String USING_OTHER_PROFILE_KEY = "usingOtherProfile";

	@Override
	protected File getSourceFile() {
		ClassLoader classLoader = getClass().getClassLoader();
		return new File(classLoader.getResource(RESOURCE_FILE_NAME).getFile());
	}

	/**
	 * <b>Given</b>: A profile contains minimal explicit assignments for the parameters
	 * used to configure the builder to build a storage
	 *
	 * <b>When</b>: A storage is built using the configuration
	 *
	 * <b>Then</b>: No exception is thrown
	 * <b>and</b> the storage is empty by default
	 * <b>and</b> no entries in the storage
	 * <b>and</b> has no limitation regarding to the capacity
	 */
	@Test
	public void shouldBuildStorageWithDefaultValues() {
		// Given
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(DEFAULT_PROFILE_KEY);

		// When
		IStorage<Long, String> storage = builder.build();

		// Then
		assertTrue(storage.isEmpty());
		assertEquals(0L, storage.entries());
		assertEquals(storage.capacity(), IStorage.NO_MAX_SIZE);
	}

	/**
	 * <b>Given</b>: A profile contains illegal value for the capacity
	 *
	 * <b>When</b>: A storage is built using the configuration
	 *
	 * <b>Then</b>: Exception is thrown
	 */
	@Test
	public void shouldThrowExceptionForInvalidProperty() {
		// Given
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(ILLEGAL_CAPACITY_PROFILE_KEY);

		// When
		Runnable action = () -> {
			IStorage<Long, String> storage = builder.build();
		};

		// Then
		assertThrows(ConstraintViolationException.class, action::run);
	}

	/**
	 * <b>Given</b>: A profile typo mistake for a property name
	 *
	 * <b>When</b>: A storage is built using the configuration
	 *
	 * <b>Then</b>: Exception is thrown
	 */
	@Test
	public void shouldThrowExceptionForInvalidProperty2() {
		// Given
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(NOT_VALID_PROPERTY_PROFILE_1_KEY);

		// When
		Runnable action = () -> {
			IStorage<Long, String> storage = builder.build();
		};

		// Then
		assertThrows(IllegalArgumentException.class, action::run);
	}

	/**
	 * <b>Given</b>: A profile typo mistake for a property name
	 *
	 * <b>When</b>: A storage is built using the configuration
	 *
	 * <b>Then</b>: Exception is thrown
	 */
	@Test
	public void shouldThrowExceptionForInvalidProperty3() {
		// Given
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(NOT_VALID_PROPERTY_PROFILE_2_KEY);

		// When
		Runnable action = () -> {
			IStorage<Long, String> storage = builder.build();
		};

		// Then
		assertThrows(IllegalArgumentException.class, action::run);
	}

	/**
	 * <b>Given</b>: A profile contains illegal value for the capacity
	 *
	 * <b>When</b>: A storage is built using the configuration
	 *
	 * <b>Then</b>: Exception is thrown
	 */
	@Test
	public void shouldValidateValueOfProperty() {
		// Given
		long capacity = 10L;
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(DEFAULT_PROFILE_KEY)
				.withConfiguration(String.join(".", StorageBuilder.CONFIGURATION_CONFIG_KEY, StorageBuilder.CAPACITY_CONFIG_KEY), capacity);

		// When
		IStorage<Long, String> storage = builder.build();

		// Then
		assertEquals(capacity, storage.capacity());
	}

	/**
	 * <b>Given</b>: A profile contains valid value for the capacity
	 *
	 * <b>When</b>: A storage is built using the configuration
	 *
	 * <b>Then</b>: Exception is thrown
	 */
	@Test
	public void shouldValidateValueOfProperty2() {
		// Given
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(VALID_CAPACITY_PROFILE_KEY);

		// When
		IStorage<Long, String> storage = builder.build();

		// Then
		Integer capacity = (Integer) builder
				.getConfiguration(String.join(".",
						StorageBuilder.CONFIGURATION_CONFIG_KEY,
						StorageBuilder.CAPACITY_CONFIG_KEY)
				);
		assertEquals(capacity, storage.capacity().intValue());
	}

	/**
	 * <b>Given</b>: A profile using another profile
	 *
	 * <b>When</b>: A storage is built using the configuration
	 *
	 * <b>Then</b>: Exception is thrown
	 */
	@Test
	public void shouldInheritProperties() {
		// Given
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(USING_OTHER_PROFILE_KEY);

		// When
		IStorage<Long, String> storage = builder.build();

		// Then
		Integer capacity = (Integer) builder
				.getConfiguration(String.join(".",
						StorageBuilder.CONFIGURATION_CONFIG_KEY,
						StorageBuilder.CAPACITY_CONFIG_KEY)
				);
		assertEquals(capacity, storage.capacity().intValue());
	}

}