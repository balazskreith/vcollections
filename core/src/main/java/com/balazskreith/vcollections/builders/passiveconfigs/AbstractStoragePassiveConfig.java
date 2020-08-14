package com.balazskreith.vcollections.builders.passiveconfigs;

import com.balazskreith.vcollections.storages.IStorage;
import javax.validation.constraints.Min;

public abstract class AbstractStoragePassiveConfig {

	public static final String VALUE_SERDE_FIELD_NAME = "valueSerDe";
	public static final String CAPACITY_FIELD_NAME = "capacity";
	public static final String KEY_SUPPLIER_FIELD_NAME = "keySupplier";

	@Min(IStorage.NO_MAX_SIZE)
	public long capacity = IStorage.NO_MAX_SIZE;

	public String keySupplier = null;

	public String valueSerDe = null;


}
