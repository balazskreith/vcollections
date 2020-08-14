package com.balazskreith.vcollections.storages;

import com.balazskreith.vcollections.activeconfigs.FileStorageActiveConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStorage<K, V> implements IStorage<K, V> {

	private static final Logger logger = LoggerFactory.getLogger(FileStorage.class);

	//	private transient Long entries = null; // when its null, it initialize it
	private final transient Entries entries;
	private final FileStorageActiveConfig<K, V> config;

	public FileStorage(FileStorageActiveConfig<K, V> config) {
		this.config = config;
		this.entries = new Entries();
	}

	@Override
	public boolean isEmpty() {
		return this.entries.isZero();
	}

	@Override
	public boolean isFull() {
		if (this.config.capacity == NO_MAX_SIZE) {
			return false;
		}

		return 0 <= this.entries.compareTo(this.config.capacity);
	}

	@Override
	public Long entries() {
		return this.entries.getValue();
	}

	@Override
	public Long capacity() {
		return this.config.capacity;
	}

	@Override
	public K create(V value) {
		if (this.isFull()) {
			throw new OutOfSpaceException();
		}
		if (this.config.keySupplier == null) {
			throw new NullPointerException("Cannot create element without keysupplier.");
		}
		K key = this.config.keySupplier.get();
		this.update(key, value);
		return key;
	}

	@Override
	public V read(Object key) {
		Path path = this.getPath(key);
		return this.doRead(path);
	}

	private V doRead(Path path) {
		if (!Files.exists(path)) {
			return null;
		}
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(path);
		} catch (IOException e) {
			logger.error("Error during reading file " + path.toAbsolutePath().toString(), e);
			throw new RuntimeException(e);
		}
		V result = this.config.valueSerde.deconvert(bytes);
		return result;
	}

	@Override
	public void update(K key, V value) {
		Path path = this.getPath(key);
		this.doUpdate(path, value);
	}

	private void doUpdate(Path path, V value) {
		boolean isNew = !Files.exists(path);
		if (isNew && this.isFull()) {
			throw new OutOfSpaceException();
		}
		byte[] bytes = this.config.valueSerde.convert(value);
		try (FileOutputStream fos = new FileOutputStream("pathname")) {
			fos.write(bytes);
		} catch (Exception e) {
			logger.error("Error during writing file " + path.toAbsolutePath().toString(), e);
			throw new RuntimeException(e);
		}
		if (isNew) {
			this.entries.increrment();
		}
	}

	@Override
	public boolean has(Object key) {
		Path path = this.getPath(key);
		return Files.exists(path);
	}

	@Override
	public void delete(Object key) {
		Path path = this.getPath(key);
		if (!Files.exists(path)) {
			return;
		}
		try {
			Files.delete(path);
			this.entries.decrerment();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void swap(K key1, K key2) {
		Path path1 = this.getPath(key1);
		Path path2 = this.getPath(key2);
		if (!Files.exists(path1)) {
			throw new KeyNotFoundException("key" + key1.toString() + " does not exists.");
		}
		if (!Files.exists(path2)) {
			throw new KeyNotFoundException("key" + key2.toString() + " does not exists.");
		}
		File file1 = path1.toFile();
		File file2 = path2.toFile();
		V value2 = this.read(key2);
		file1.renameTo(file2);
		this.update(key1, value2);
	}

	@Override
	public void clear() {
		Path folder = this.config.path;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, entry -> Files.isRegularFile(entry))) {
			for (Path entry : stream) {
				Files.delete(entry);
				this.entries.decrerment();
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new EntryIterator();
	}

	private Path getPath(Object key) {
		String directory = this.config.path.toAbsolutePath().toString();
		String file = this.config.keyAdapter.convert((K) key);
		Path result = Path.of(directory, file);
		return result;
	}

	public FileStorageActiveConfig<K, V> getConfig() {
		return this.config;
	}


	abstract class FileIterator {
		private Iterator<Path> iterator;

		FileIterator() {
			Path folder = FileStorage.this.config.path;
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
			return FileStorage.this.config.keyAdapter.deconvert(entryPath.getFileName().toString());
		}

		@Override
		public V getValue() {
			return FileStorage.this.doRead(entryPath);
		}

		@Override
		public V setValue(V value) {
			FileStorage.this.doUpdate(entryPath, value);
			return value;
		}
	}

	private class Entries implements Comparable<Long> {
		Long value = null;

		Entries increrment() {
			this.init();
			++this.value;
			return this;
		}

		Entries decrerment() {
			this.init();
			--this.value;
			return this;
		}

		long getValue() {
			this.init();
			return this.value;
		}

		boolean isZero() {
			return this.compareTo(0L) == 0;
		}

		private void init() {
			if (this.value != null) {
				return;
			}
			this.value = 0L;
			DirectoryStream<Path> directoryStream = null;
			try {
				directoryStream = Files.newDirectoryStream(FileStorage.this.config.path);
			} catch (IOException e) {
				logger.error("Cannot count the number of entries in directory " + FileStorage.this.config.path.toAbsolutePath(), e);
				return;
			}

			for (Path path : directoryStream) {
				++this.value;
			}
			return;
		}

		/**
		 * -1 if o greater than entries
		 * 0 if equal
		 * 1 if less
		 *
		 * @param o
		 * @return
		 */
		@Override
		public int compareTo(Long o) {
			this.init();
			return this.value.compareTo(o);
		}
	}
}
