import java.util.UUID;

public interface IVLinkedListNode<T> {
//	UUID getUUID();

	UUID getNextUUID();

	UUID getPrevUUID();

	T getValue();

}
