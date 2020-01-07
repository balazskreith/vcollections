package vcollections.builders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlToJsonConverter {

	String convertYamlToJson(String yaml) throws JsonProcessingException {
		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Object obj = yamlReader.readValue(yaml, Object.class);

		ObjectMapper jsonWriter = new ObjectMapper();
		return jsonWriter.writeValueAsString(obj);
	}
}
