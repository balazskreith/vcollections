package com.balazskreith.vcollections.builders;

import static org.junit.jupiter.api.Assertions.*;
import com.balazskreith.vcollections.storages.IMapper;
import com.balazskreith.vcollections.storages.IStorage;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileStorageBuilderTest extends AbstractBuilderTester {

	private static final String RESOURCE_FILE_NAME = "file_storage_example.yaml";
	private static final String MINIMAL_CONFIGURATION_PROFILE = "minimalConfigurationProfile";
	private static final String USING_OTHER_CONFIGURATION_PROFILE = "usingOtherConfigurationProfile";
	private static final String EXPLICIT_KEYMAPPER_CONFIGURATION_PROFILE = "explicitKeyMapperProfile";
	private static final String WRONG_CONFIGURATION_PROFILE = "wrongConfigurationProfile";


	@TempDir
	File tempDir;

	public static class MyMapper implements IMapper<Long, String> {

		@Override
		public String encode(Long value) {
			return value.toString();
		}

		@Override
		public Long decode(String value) {
			return Long.parseLong(value);
		}
	}

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
				.getStorageBuilderFor(MINIMAL_CONFIGURATION_PROFILE)
				.withConfiguration(String.join(".", StorageBuilder.CONFIGURATION_CONFIG_KEY, FileStorageBuilder.PATH_CONFIG_KEY), tempDir.getAbsolutePath());

		// When
		IStorage<Long, String> storage = builder.build();

		// Then
		assertTrue(storage.isEmpty());
		assertEquals(0L, storage.entries());
		assertEquals(storage.capacity(), IStorage.NO_MAX_SIZE);
	}

	/**
	 * <b>Given</b>: A profile use another profile
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
	public void shouldBuildStorageUsingAnotherProfile() {
		// Given
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(USING_OTHER_CONFIGURATION_PROFILE)
				.withConfiguration(String.join(".", StorageBuilder.CONFIGURATION_CONFIG_KEY, FileStorageBuilder.PATH_CONFIG_KEY), tempDir.getAbsolutePath());

		// When
		IStorage<Long, Double> storage = builder.build();

		// Then
		assertTrue(storage.isEmpty());
		assertEquals(0L, storage.entries());
		assertEquals(storage.capacity(), IStorage.NO_MAX_SIZE);
	}

	/**
	 * <b>Given</b>: A profile contains explicit assignments for the keymapper
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
	public void shouldBuildUsingExplicitKeyMapper() {
		// Given
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(EXPLICIT_KEYMAPPER_CONFIGURATION_PROFILE)
				.withConfiguration(String.join(".", StorageBuilder.CONFIGURATION_CONFIG_KEY, FileStorageBuilder.PATH_CONFIG_KEY), tempDir.getAbsolutePath());

		// When
		IStorage<Long, String> storage = builder.build();

		// Then
		assertTrue(storage.isEmpty());
		assertEquals(0L, storage.entries());
		assertEquals(storage.capacity(), IStorage.NO_MAX_SIZE);
	}

	/**
	 * <b>Given</b>: A profile contains configuration for keymapper and keytype
	 *
	 * <b>When</b>: A storage is built using the configuration
	 *
	 * <b>Then</b>: exception is thrown
	 */
	@Test
	public void shouldThrowException() {
		// Given
		IStorageBuilder builder = this
				.getStorageProfiles()
				.getStorageBuilderFor(WRONG_CONFIGURATION_PROFILE)
				.withConfiguration(String.join(".", StorageBuilder.CONFIGURATION_CONFIG_KEY, FileStorageBuilder.PATH_CONFIG_KEY), tempDir.getAbsolutePath());


		// When
		Runnable action = () -> {
			IStorage<Long, String> storage = builder.build();
		};

		// Then
		assertThrows(InvalidConfigurationException.class, action::run);
	}

}