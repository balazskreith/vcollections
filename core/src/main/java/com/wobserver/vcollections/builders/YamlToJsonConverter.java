package com.wobserver.vcollections.builders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.function.Function;

/**
 * Represents A YamlConverter to convert Yaml file to Json.
 */
public class YamlToJsonConverter implements Function<String, String> {

	public String apply(String yaml)  {
		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Object obj = null;
		try {
			obj = yamlReader.readValue(yaml, Object.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		ObjectMapper jsonWriter = new ObjectMapper();
		try {
			return jsonWriter.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
