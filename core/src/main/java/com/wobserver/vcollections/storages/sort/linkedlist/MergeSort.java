package com.wobserver.vcollections.storages.sort.linkedlist;

import com.wobserver.vcollections.storages.IStorage;
import com.wobserver.vcollections.storages.sort.ISorter;
import java.util.Comparator;

public class MergeSort<T> implements ISorter {

	private IStorage<Long, T> storage;
	private Comparator<? super T> comparator;

	public MergeSort(IStorage<Long, T> storage, Comparator<? super T> comparator) {
		this.storage = storage;
		this.comparator = comparator;
	}

	@Override
	public void run() {

	}
}
