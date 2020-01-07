package vcollections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vcollections.storages.IStorage;
import vcollections.storages.MemoryStorage;

class VArrayListTest implements ListTest<VArrayList<String>> {

	private List<String> makeList(long maxCapacity, String... items) {
		HashMap<Long, String> initialItems = new HashMap<>();
		if (items != null) {
			for (Long i = 0L; i < items.length; ++i) {
				String value = items[i.intValue()];
				initialItems.put(i, value);
			}
		}
		IStorage<Long, String> storage = new MemoryStorage<>(null, initialItems, maxCapacity);
		return new VArrayList<>(storage);
	}


	public List<String> makeList(String... items) {
		return this.makeList(IStorage.NO_MAX_SIZE, items);
	}

	@Test
	public void t() throws JsonProcessingException {
		String yamlString =
				"data_lists:\n" +
						"      list1:  \n" +
						"        - AA: true\n" +
						"          BB: true\n" +
						"          CC: \"value\"\n" +
						"        - AA: false\n" +
						"          BB: true\n" +
						"          CC: \"value2\"";

		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		Map<String, Object> fileMap = mapper.readValue(
				yamlString,
				new TypeReference<Map<String, Object>>() {
				});
		System.out.println(fileMap.get("data_lists"));
	}

}