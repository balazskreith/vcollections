package com.wobserver.vcollections.builders;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractBuilderTester {

	private StorageProfiles storageProfiles = null;

	/**
	 * Reset the configuration built so far
	 */
	@BeforeEach
	public void setup() {

		File file = this.getSourceFile();
		this.storageProfiles = new StorageProfiles();
		try {
			this.storageProfiles.addYamlFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	protected StorageProfiles getStorageProfiles() {
		return this.storageProfiles;
	}


	protected abstract File getSourceFile();

}