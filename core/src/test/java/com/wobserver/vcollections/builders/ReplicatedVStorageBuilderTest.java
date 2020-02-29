package com.wobserver.vcollections.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.wobserver.vcollections.storages.IStorage;
import java.io.File;
import org.junit.jupiter.api.Test;

class ReplicatedVStorageBuilderTest extends AbstractBuilderTester {

	private static final String RESOURCE_FILE_NAME = "replicated_vstorage_example.yaml";
	private static final String MINIMAL_CONFIGURATION_PROFILE = "minimalConfigurationProfile";

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
				.withStorageProfile(MINIMAL_CONFIGURATION_PROFILE)
				.makeBuilder();

		// When
		IStorage<Long, String> storage = builder.build();

		// Then
		assertTrue(storage.isEmpty());
		assertEquals(0L, storage.entries());
		assertEquals(storage.capacity(), IStorage.NO_MAX_SIZE);
	}


}