package com.balazskreith.vcollections;

import com.balazskreith.vcollections.adapters.SerDe;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;

public class VLinkedListNodeKeysJacksonSerDe<K, V> implements VLinkedListNodeKeysSerDe<K, V> {

	private static final String NEXT_FIELD_NAME = "next";
	private static final String ACTUAL_FIELD_NAME = "actual";
	private static final String PREV_FIELD_NAME = "prev";
	private static final String VALUE_FIELD_NAME = "value";

	private final ObjectMapper mapper;
	private final SerDe<K> keySerDe;
	private final SerDe<V> valueSerDe;

	public JacksonVLinkedListNodeSerDe(SerDe<K> keySerDe, SerDe<V> valueSerDe) {
		this.mapper = new ObjectMapper();
		this.keySerDe = keySerDe;
		this.valueSerDe = valueSerDe;
		SimpleModule module = new SimpleModule();
		module.addSerializer(VLinkedListNode.class, new VLinkedListNodeJsonDeserializer());
		module.addDeserializer(VLinkedListNode.class, new VLinkedListNodeJsonDeserializer());
		mapper.registerModule(module);
	}

	@Override
	public VLinkedListNode<K, V> deserialize(byte[] data) throws IOException {
		VLinkedListNode<K, V> result = this.mapper.readValue(data, VLinkedListNode.class);
		return result;
	}

	@Override
	public byte[] serialize(VLinkedListNode<K, V> data) throws IOException {
		byte[] result = this.mapper.writeValueAsBytes(data);
		return new byte[0];
	}

	public class VLinkedListNodeJsonSerializer extends JsonSerializer<VLinkedListNode<K, V>> {

		@Override
		public void serialize(VLinkedListNode<K, V> node, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeStartObject();
			byte[] nextBytes = JacksonVLinkedListNodeSerDe.this.keySerDe.serialize(node.next);
			byte[] prevBytes = JacksonVLinkedListNodeSerDe.this.keySerDe.serialize(node.prev);
			byte[] actualBytes = JacksonVLinkedListNodeSerDe.this.keySerDe.serialize(node.actual);
			byte[] valueBytes = JacksonVLinkedListNodeSerDe.this.valueSerDe.serialize(node.value);
			gen.writeBinaryField(NEXT_FIELD_NAME, nextBytes);
			gen.writeBinaryField(PREV_FIELD_NAME, prevBytes);
			gen.writeBinaryField(ACTUAL_FIELD_NAME, actualBytes);
			gen.writeBinaryField(VALUE_FIELD_NAME, valueBytes);
			gen.writeEndObject();
		}
	}

	public class VLinkedListNodeJsonDeserializer extends JsonDeserializer<VLinkedListNode<K, V>> {

		@Override
		public VLinkedListNode<K, V> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			VLinkedListNode<K, V> result = new VLinkedListNode<>();
			ObjectCodec oc = jp.getCodec();
			JsonNode node = oc.readTree(jp);
			byte[] nextBytes = node.get(NEXT_FIELD_NAME).asText().getBytes();
			byte[] actualBytes = node.get(ACTUAL_FIELD_NAME).asText().getBytes();
			byte[] prevBytes = node.get(PREV_FIELD_NAME).asText().getBytes();
			byte[] valueBytes = node.get(VALUE_FIELD_NAME).asText().getBytes();
			result.prev = JacksonVLinkedListNodeSerDe.this.keySerDe.deconvert(prevBytes);
			result.actual = JacksonVLinkedListNodeSerDe.this.keySerDe.deconvert(actualBytes);
			result.next = JacksonVLinkedListNodeSerDe.this.keySerDe.deconvert(nextBytes);
			result.value = JacksonVLinkedListNodeSerDe.this.valueSerDe.deconvert(valueBytes);
			return result;
		}
	}
}
