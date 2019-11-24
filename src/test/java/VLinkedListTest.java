import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import storages.IStorage;
import storages.MemoryStorage;

class VLinkedListTest implements ListTest<VLinkedList<String>>, DequeTest<VLinkedList<String>> {

	private IVLinkedListNode<String> makeNode(UUID uuid, UUID prev, UUID next, String value) {
		return new IVLinkedListNode<String>() {

			@Override
			public UUID getNextUUID() {
				return next;
			}

			@Override
			public UUID getPrevUUID() {
				return prev;
			}

			@Override
			public String getValue() {
				return value;
			}
		};
	}

	private VLinkedList<String> makeLinkedList(long maxCapacity, String... items) {
		HashMap<UUID, IVLinkedListNode<String>> initialItems = new HashMap<>();
		List<UUID> uuids = Stream.iterate(UUID.randomUUID(), i -> UUID.randomUUID()).limit(items.length).collect(Collectors.toList());
		UUID first = null;
		if (items != null) {
			for (int i = 0; i < items.length; ++i) {
				String value = items[i];
				IVLinkedListNode<String> node;
				if (i == 0 && i == items.length - 1) {
					node = makeNode(uuids.get(i), null, null, value);
					first = uuids.get(0);
				} else if (i == 0) {
					node = makeNode(uuids.get(i), null, uuids.get(i + 1), value);
					first = uuids.get(0);
				} else if (i == items.length - 1) {
					node = makeNode(uuids.get(i), uuids.get(i - 1), null, value);
				} else {
					node = makeNode(uuids.get(i), uuids.get(i - 1), uuids.get(i + 1), value);
				}

				initialItems.put(uuids.get(i), node);
			}
		}
		IStorage<UUID, IVLinkedListNode<String>> storage = new MemoryStorage<>(initialItems, maxCapacity);
		VLinkedList<String> result;
		if (first == null) {
			result = new VLinkedList<>(storage);
		} else {
			result = new VLinkedList<String>(storage, first);
		}
		return result;
	}

	@Override
	public Deque<String> makeDeque(String... items) {
		return this.makeLinkedList(IStorage.NO_MAX_SIZE, items);
	}

	public List<String> makeList(String... items) {
		return this.makeLinkedList(IStorage.NO_MAX_SIZE, items);
	}
}