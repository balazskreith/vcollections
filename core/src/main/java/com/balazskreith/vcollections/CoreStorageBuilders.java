package com.balazskreith.vcollections;

import com.balazskreith.vcollections.builders.CachedStorageBuilder;
import com.balazskreith.vcollections.builders.ClusteredStoragesBuilder;
import com.balazskreith.vcollections.builders.MemoryStorageBuilder;

public class CoreStorageBuilders {

	/**
	 * @return
	 */
	public CachedStorageBuilder getCachedStorageBuilder() {
		return new CachedStorageBuilder();
	}

	public ClusteredStoragesBuilder getClusteredStorageBuilder() {

	}

	public MemoryStorageBuilder getMemoryStorageBuilder() {

	}


}
