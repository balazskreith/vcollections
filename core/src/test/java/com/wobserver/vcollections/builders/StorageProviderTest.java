package com.wobserver.vcollections.builders;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.wobserver.vcollections.storages.IStorage;

@Disabled
class StorageProviderTest extends AbstractBuilderTest {

	private String generateYaml(Map<String, Object> content) throws IOException {

		YAMLFactory yamlFactory = new YAMLFactory();
		YAMLMapper mapper = new YAMLMapper(yamlFactory);
		return mapper.writeValueAsString(content).trim();
//
//
//		try (StringWriter out = new StringWriter()) {
//			YAMLGenerator yamlGenerator = yamlFactory.createGenerator(out);
//			yamlGenerator.writeStartObject();
//			yamlGenerator.writeObjectField("firstName", "Duke");
//			yamlGenerator.writeObjectField("lastName", "Java");
//			yamlGenerator.writeObjectField("age", 18);
//			yamlGenerator.writeObjectField("streetAddress", "100 Internet Dr");
//			yamlGenerator.writeObjectField("city", "JavaTown");
//			yamlGenerator.writeObjectField("state", "JA");
//			yamlGenerator.writeObjectField("postalCode", "12345");
//			yamlGenerator.writeFieldName("phoneNumbers");
//			yamlGenerator.writeStartArray();
//			yamlGenerator.writeStartObject();
//			yamlGenerator.writeObjectField("Mobile", "111-111-1111");
//			yamlGenerator.writeEndObject();
//			yamlGenerator.writeStartObject();
//			yamlGenerator.writeObjectField("Home", "222-222-2222");
//			yamlGenerator.writeEndObject();
//			yamlGenerator.writeEndArray();
//			yamlGenerator.writeEndObject();
//
//			yamlGenerator.flush();
//			yamlGenerator.close();
//		}
	}

	@Test
	public void test() throws IOException {
		String yamlString = "storages:\n" +
				"  - key: customers\n" +
				"    builder: cachedStorage\n" +
				"    configuration:\n" +
				"      keyType: java.lang.String\n" +
				"      subset:\n" +
				"        builder: memoryStorage\n" +
				"      superset:\n" +
				"        builder: memoryStorage\n" +
				"  - key: sessions\n" +
				"    builder: lruMemoryStorage\n" +
				"    configuration:\n" +
				"      retentionInMs: 10000\n" +
				"      capacity: 10000";

		StorageProvider storageProvider = new StorageProvider();
		storageProvider.addYamlString(yamlString);
		IStorage<String, Integer> customers = storageProvider.get("customers");
//		Object result = this.mapper.readValue(yaml, Map.class);
	}

	@Test
	public void shouldProvideStorages() {
		String storageKey1 = "storage1";
		String storageKey2 = "storage2";
		Map<String, Object> configuration = makeMap(
				StorageProvider.KEY_CONFIG_KEY, storageKey1,
				StorageBuilder.BUILDER_CONFIG_KEY, "MemoryStorageBuilder",
				StorageBuilder.CONFIGURATION_CONFIG_KEY, MemoryStorageBuilderTest.createConfig("uuid", null),
				StorageProvider.KEY_CONFIG_KEY, storageKey2,
				StorageBuilder.BUILDER_CONFIG_KEY, "CachedStorageBuilder",
				StorageBuilder.CONFIGURATION_CONFIG_KEY, CachedStorageBuilderTest.createConfig("com.lang.String", 3L)
		);


	}
}