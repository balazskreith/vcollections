import java.util.HashMap;
import java.util.List;
import storages.IStorage;
import storages.MemoryStorage;

class VArrayListTest implements ListTest<VArrayList<String>> {

	private List<String> makeList(long maxCapacity, String... items) {
		HashMap<Long, String> initialItems = new HashMap<>();
		if (items != null) {
			for (Long i = 0L; i < items.length; ++i) {
				String value = items[i.intValue()];
				initialItems.put(i, value);
			}
		}
		IStorage<Long, String> storage = new MemoryStorage<>(initialItems, maxCapacity);
		return new VArrayList<>(storage);
	}


	public List<String> makeList(String... items) {
		return this.makeList(IStorage.NO_MAX_SIZE, items);
	}


}