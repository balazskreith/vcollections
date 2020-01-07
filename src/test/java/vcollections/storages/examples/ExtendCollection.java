package vcollections.storages.examples;

import org.junit.jupiter.api.Test;

public class ExtendCollection {
	/**
	 * <b>Situation</b>: A collection in a legacy code, has more, and more items occasionally.
	 * While to the service the memory consumption is always increased, it is not used 99% of the time.
	 *
	 * <b>Solution</b>: We can use a sequential storage where the limitation for the memory is given, and
	 * the overall storage limitation is extended
	 */
	@Test
	public void extendStorage() {

	}

	/**
	 * <b>Situation</b> Given a collection, which collects business critical information, but if the application
	 * crashes, the data are gone.
	 *
	 * <b>Solution</b>: We can replicate the storage and save the items into two places, where the replica is persistent
	 *
	 * <b>Note 1</b>: Up until the point the application does not crash, it retrieves items from the primary storage,
	 * and only saving to the sercondory one
	 *
	 * <b>Note 2</b>: Up until synchronization is not solved, at restart the retrieval of the items
	 * from the secondory storage is the responsibility of the application.
	 */
}
