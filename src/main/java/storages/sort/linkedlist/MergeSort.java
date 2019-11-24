package storages.sort.linkedlist;

import java.util.Comparator;
import storages.IStorage;
import storages.sort.ISorter;

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
