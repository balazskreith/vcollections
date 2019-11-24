import java.util.PriorityQueue;
import storages.IStorage;

public class VPriorityQueue<T extends Comparable<T>> {
	private PriorityQueue<T> queue;

	private IStorage<Long, T> storage;
}
