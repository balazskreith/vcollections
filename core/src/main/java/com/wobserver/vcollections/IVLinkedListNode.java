package com.wobserver.vcollections;

import java.util.UUID;

/**
 * Represents a common interface for any node belongs to a {@link VLinkedList}
 * @param <T> The type of the item
 */
public interface IVLinkedListNode<T> {
//	UUID getUUID();

	/**
	 * The unique id of the next item in the linked list
	 * @return The {@link UUID} of the next item in the {@link VLinkedList }
	 */
	UUID getNextUUID();

	/**
	 * The unique id of the previous item in the linked list
	 * @return The {@link UUID} of the previous item in the {@link VLinkedList }
	 */
	UUID getPrevUUID();

	/**
	 * The value of the node
	 * @return The value of the node
	 */
	T getValue();

}
