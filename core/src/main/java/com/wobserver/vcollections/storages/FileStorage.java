package com.wobserver.vcollections.storages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;
import com.wobserver.vcollections.keygenerators.IKeyGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class FileStorage<K, V> implements IStorage<K, V>, IAccessKeyGenerator<K> {

	private long capacity;
	private final String directory;
	private final ObjectMapper valueMapper;
	private final Class<V> valueType;
	private IKeyGenerator<K> keyGenerator;
	private long entries;
	private IMapper<K, String> keyMapper;

	public FileStorage(IMapper<K, String> keyMapper, Class<V> valueType, ObjectMapper valueMapper, String directory, IKeyGenerator<K> keyGenerator, Long capacity) throws IOException {
		this.keyMapper = keyMapper;
		this.valueType = valueType;
		this.valueMapper = valueMapper;
		this.directory = directory;
		this.capacity = capacity;
		this.keyGenerator = keyGenerator;

		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(this.directory));

		for (Path path : directoryStream) {
			++this.entries;
		}
	}

	@Override
	public boolean isEmpty() {
		return this.entries == 0;
	}

	@Override
	public boolean isFull() {
		if (this.capacity == NO_MAX_SIZE) {
			return false;
		}
		return this.capacity <= this.entries;
	}

	@Override
	public Long entries() {
		return this.entries;
	}

	@Override
	public Long capacity() {
		return this.capacity;
	}

	@Override
	public K create(V value) {
		if (this.isFull()) {
			throw new OutOfSpaceException();
		}
		if (this.keyGenerator == null) {
			throw new NullPointerException("Create operation without keyGenerator is not supported.");
		}
		K key = this.keyGenerator.get();
		this.update(key, value);
		return key;
	}

	@Override
	public V read(Object key) {
		Path folder = Paths.get(this.directory);
		String fileName = this.getFileName(key);
		Path destination = folder.resolve(fileName);
		if (!Files.exists(destination)) {
			return null;
		}
		try {
			return this.valueMapper.readValue(destination.toFile(), this.valueType);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void update(K key, V value) {
		Path folder = Paths.get(this.directory);
		String fileName = this.keyMapper.encode(key);
		Path destination = folder.resolve(fileName);
		boolean isNew = !Files.exists(destination);
		if (isNew && this.isFull()) {
			throw new OutOfSpaceException();
		}
		try {
			this.valueMapper.writeValue(destination.toFile(), value);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		if (isNew) {
			++this.entries;
		}
	}

	@Override
	public boolean has(Object key) {
		Path folder = Paths.get(this.directory);
		String fileName = this.getFileName(key);
		return Files.exists(folder.resolve(fileName));
	}

	@Override
	public void delete(Object key) {
		Path folder = Paths.get(this.directory);
		String fileName = this.getFileName(key);
		Path destination = folder.resolve(fileName);
		if (!Files.exists(destination)) {
			return;
		}
		try {
			Files.delete(destination);
			--this.entries;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void swap(K key1, K key2) {
		Path folder = Paths.get(this.directory);
		String fileName1 = this.getFileName(key1);
		String fileName2 = this.getFileName(key2);
		Path path1 = folder.resolve(fileName1);
		Path path2 = folder.resolve(fileName2);
		if (!Files.exists(path1)) {
			throw new KeyNotFoundException("key" + key1.toString() + " does not exists.");
		}
		if (!Files.exists(path2)) {
			throw new KeyNotFoundException("key" + key2.toString() + " does not exists.");
		}
		File file1 = path1.toFile();
		File file2 = path2.toFile();
		V value1 = null;
		try {
			value1 = this.valueMapper.readValue(file1, this.valueType);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		file2.renameTo(file1);
		try {
			this.valueMapper.writeValue(file2, value1);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void clear() {
		Path folder = Paths.get(this.directory);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, entry -> Files.isRegularFile(entry))) {
			for (Path entry : stream) {
				Files.delete(entry);
				--this.entries;
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new EntryIterator();
	}


	private String getFileName(Object key) {
		// TODO: add possibility to define your own!
		if (key == null) {
			return new UUID(0L, 0L).toString();
		}
		return key.toString();
	}

	@Override
	public void setKeyGenerator(IKeyGenerator<K> value) {
		this.keyGenerator = value;
	}

	@Override
	public IKeyGenerator<K> getKeyGenerator() {
		return this.keyGenerator;
	}


	abstract class FileIterator {
		private Iterator<Path> iterator;

		FileIterator() {
			Path folder = Paths.get(FileStorage.this.directory);
			try {
				this.iterator = Files.newDirectoryStream(folder, entry -> Files.isRegularFile(entry)).iterator();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		}

		public final boolean hasNext() {
			return this.iterator.hasNext();
		}

		public final Map.Entry<K, V> nextEntry() {
			Path path = this.iterator.next();
			return new FileStorage.FileEntry(path);
		}

		public final void remove() {
			this.iterator.remove();
		}
	}

//	final class KeyIterator extends FileStorage<K, V>.FileIterator
//			implements Iterator<K> {
//		public final K next() {
//			return nextEntry().getKey();
//		}
//	}
//
//	final class ValueIterator extends FileStorage<K, V>.FileIterator
//			implements Iterator<V> {
//		public final V next() {
//			return nextEntry().getValue();
//		}
//	}

	final class EntryIterator extends FileStorage<K, V>.FileIterator
			implements Iterator<Map.Entry<K, V>> {
		public final Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	// TODO: using objectfactory
	private final class FileEntry implements Map.Entry<K, V> {

		private final Path entryPath;

		FileEntry(Path entryPath) {
			this.entryPath = entryPath;
		}

		@Override
		public K getKey() {
			return FileStorage.this.keyMapper.decode(entryPath.getFileName().toString());
		}

		@Override
		public V getValue() {
			try {
				return FileStorage.this.valueMapper.readValue(entryPath.toFile(), FileStorage.this.valueType);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		}

		@Override
		public V setValue(V value) {
			try {
				V oldValue = FileStorage.this.valueMapper.readValue(entryPath.toFile(), FileStorage.this.valueType);
				FileStorage.this.valueMapper.writeValue(entryPath.toFile(), value);
				return oldValue;
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}
}
