package com.balazskreith.vcollections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.balazskreith.vcollections.keygenerators.SequentialLongGenerator;
import com.balazskreith.vcollections.storages.IStorage;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.balazskreith.vcollections.storages.DefaultMapperFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VArrayListTest implements ListTest<String, VArrayList<Long, String>> {

	private List<String> makeList(long maxCapacity, String... items) {
		HashMap<Long, String> initialItems = new HashMap<>();
		if (items != null) {
			for (Long i = 0L; i < items.length; ++i) {
				String value = items[i.intValue()];
				initialItems.put(i, value);
			}
		}
		MemoryStorage<Long, String> storage = new MemoryStorage<>(new SequentialLongGenerator(), initialItems, maxCapacity);
		return new VArrayList<Long, String>(storage, DefaultMapperFactory.make(Long.class, Long.class));

	}


	public List<String> makeList(String... items) {
		return this.makeList(IStorage.NO_MAX_SIZE, items);
	}

	@Override
	public String toItem(String item) {
		return item;
	}

	@Override
	public List<String> asArrayList(String... items) {
		return Arrays.asList(items);
	}

	@Override
	public String getValue(String value) {
		return value;
	}

	@Override
	public String setValue(String item, String value) {
		item = value;
		return item;
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