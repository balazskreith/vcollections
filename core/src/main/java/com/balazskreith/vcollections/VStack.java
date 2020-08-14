//package com.balazskreith.vcollections;
//
//import java.util.Stack;
//import java.util.UUID;
//
//public class VStack<T> extends VLinkedList<Long, T> {
//	private Stack<T> stack;
//
//	public VStack(IStorage<UUID, IVLinkedListNode<T>> storage) {
//		super(storage);
//	}
//
//	@Override
//	public T pop() {
//		return this.removeLast();
//	}
//}
