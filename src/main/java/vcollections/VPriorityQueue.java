package vcollections;

import java.util.PriorityQueue;
import vcollections.storages.IStorage;

public class VPriorityQueue<T extends Comparable<T>> {
	private PriorityQueue<T> queue;

	private IStorage<Long, T> storage;
}
