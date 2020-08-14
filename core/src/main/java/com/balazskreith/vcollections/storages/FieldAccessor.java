package com.balazskreith.vcollections.storages;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FieldAccessor<TObject, TKey> {
	private final BiConsumer<TObject, TKey> setter;
	private final Function<TObject, TKey> getter;

	public FieldAccessor(BiConsumer<TObject, TKey> setter, Function<TObject, TKey> getter) {
		this.setter = setter;
		this.getter = getter;
	}

	public FieldAccessor(Function<TObject, TKey> getter) {
		this.setter = (obj, val) -> {throw new UnsupportedOperationException();};
		this.getter = getter;
	}

	public void set(TObject object, TKey value) {
		this.setter.accept(object, value);
	}
	
	public TKey get(TObject object) {
		return this.getter.apply(object);
	}
}
