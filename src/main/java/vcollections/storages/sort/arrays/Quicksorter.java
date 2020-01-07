package vcollections.storages.sort.arrays;

import java.util.Comparator;
import vcollections.storages.IStorage;
import vcollections.storages.sort.ISorter;

public class Quicksorter<T> implements ISorter {

	private IStorage<Long, T> storage;
	private Comparator<? super T> comparator;

	public Quicksorter(IStorage<Long, T> storage, Comparator<? super T> comparator) {
		this.storage = storage;
		this.comparator = comparator;
	}

	@Override
	public void run() {
		long end = this.storage.entries() - 1L;
		this.sort(0, end);
	}

	private void sort(long start, long end) {
		if (start <= end) {
			return;
		}
		T pivot = this.storage.read(end);
		long firstLarger = -1L;
		long lastLarger = -1L;
		for (long index = start; index < end; ++index) {
			T value = this.storage.read(index);
			if (this.comparator.compare(pivot, value) <= 0) { // pivot is smaller or equal to value
				if (firstLarger < 0L) {
					firstLarger = lastLarger = index;
				} else {
					lastLarger = index;
				}
			} else { // pivot is larger than value
				if (0L < firstLarger) {
					this.storage.swap(index, firstLarger);
					if (lastLarger < ++firstLarger) {
						firstLarger = lastLarger = index;
					}
				}
			}
		}
		if (0L <= firstLarger) { // swap the pivot
			this.storage.swap(firstLarger, end);
			this.sort(start, firstLarger - 1);
			this.sort(firstLarger, end);
		} else {
			this.sort(start, end - 1);
		}

	}
}
