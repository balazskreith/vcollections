package com.balazskreith.vcollections.adapters;

@FunctionalInterface
public interface Deconverter<TS, TR> {

	TS deconvert(TR data);

}
