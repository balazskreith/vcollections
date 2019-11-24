import java.util.Stack;
import java.util.UUID;
import storages.IStorage;

public class VStack<T> extends VLinkedList<T> {
	private Stack<T> stack;

	public VStack(IStorage<UUID, IVLinkedListNode<T>> storage) {
		super(storage);
	}

	@Override
	public T pop() {
		return this.removeLast();
	}
}
