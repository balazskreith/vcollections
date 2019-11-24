package storages;

public interface ICache {
	void flush();

	long hits();

	long misses();

	void doCache(boolean onCreate, boolean onRead, boolean onUpdate);

}
