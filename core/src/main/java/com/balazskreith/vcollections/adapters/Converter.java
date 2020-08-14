package com.balazskreith.vcollections.adapters;

@FunctionalInterface
public interface Converter<TS, TR> {

	TR convert(TS data);


}
