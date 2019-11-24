package storages;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SimpleKeyGeneratorFactory<T> {

	public static <K> Supplier<K> make(Class<K> type) {
		return new SimpleKeyGeneratorFactory<K>(type).make();
	}

	Class<T> type;

	public SimpleKeyGeneratorFactory(Class<T> type) {
		this.type = type;
	}

	public Supplier<T> make() {
		if (type.isAssignableFrom(Long.class)) {
			return new Supplier<T>() {
				private AtomicReference<Long> nextHolder = new AtomicReference<>(1L);

				@Override
				public T get() {
					T result = (T) nextHolder.getAndUpdate(v -> v + 1);
					return result;
				}
			};
		}
		if (type.isAssignableFrom(String.class)) {
			return () -> (T) UUID.randomUUID().toString();
		}
		if (type.isAssignableFrom(UUID.class)) {
			return () -> (T) UUID.randomUUID();
		}
		if (type.isAssignableFrom(Integer.class)) {
			return new Supplier<T>() {
				private AtomicReference<Integer> nextHolder = new AtomicReference<>(1);

				@Override
				public T get() {
					T result = (T) nextHolder.getAndUpdate(v -> v + 1);
					return result;
				}
			};
		}
		if (type.isAssignableFrom(byte[].class)) {
			return () -> (T) UUID.randomUUID().toString().getBytes();
		}
		return null;
	}
}
