package com.balazskreith.vcollections.utils;

import java.util.function.Supplier;

public class SystemClockProvider implements Supplier<Long> {
	@Override
	public Long get() {
		return System.currentTimeMillis();
	}
}
