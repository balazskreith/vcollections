package com.balazskreith.vcollections.adapters;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class PrimitiveAdapterFactory<TS, TR> {

	private static Map<String, Adapter> mappers = makeAdapters();
	private static Set<String> primitiveTypes = Set.of(
			Boolean.class.getName(),
			String.class.getName(),
			byte[].class.getName(),
			char[].class.getName(),
			Long.class.getName(),
			Short.class.getName(),
			Integer.class.getName(),
			Float.class.getName(),
			Double.class.getName(),
			UUID.class.getName()
	);

	public static <S, R> Adapter<S, R> makeAdapter(Function<S, R> encoder, Function<R, S> decoder) {
		return new Adapter<S, R>() {
			private final Function<S, R> encode = encoder;
			private final Function<R, S> decode = decoder;

			@Override
			public R convert(S value) {
				return this.encode.apply(value);
			}

			@Override
			public S deconvert(R value) {
				return this.decode.apply(value);
			}
		};
	}


	public static <T> boolean isPrimitiveType(Class<T> type) {
		return isPrimitiveType(type.getName());
	}

	public static boolean isPrimitiveType(String typeName) {
		return primitiveTypes.contains(typeName);
	}


	public static <TSS, TRS> Adapter<TSS, TRS> make(Class<TSS> sourceType, Class<TRS> resutType) {
		return new PrimitiveAdapterFactory<TSS, TRS>(sourceType, resutType).make();
	}

	private Class<TS> sourceType;
	private Class<TR> resultType;

	public PrimitiveAdapterFactory(Class<TS> sourceType, Class<TR> resultType) {
		this.sourceType = sourceType;
		this.resultType = resultType;
		if (mappers == null) {
			mappers.putAll(this.makeAdapters());
		}
	}

	public Adapter<TS, TR> make() {
		String key = this.getKey(sourceType, resultType);
		Adapter<TS, TR> result = (Adapter<TS, TR>) this.mappers.get(key);
		if (result == null) {
			throw new RuntimeException(this.getClass().getName() +
					" does not found mapper for types " + sourceType.getName() +
					", and for " + resultType.getName() + ". May it happen that they are not primitive types?! ");
		}
		return result;
	}


	private static String getKey(Class sourceType, Class resultTye) {
		return String.join(" <-> ", sourceType.getName(), resultTye.getName());
	}

	private static Map<String, Adapter> makeAdapters() {
		Map<String, Adapter> result = new HashMap<>();

		// ------------------ FROM: Long ----------------
		Function<Long, byte[]> longToBytes = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
			buffer.putLong(v);
			return buffer.array();
		};
		Function<byte[], Long> bytesToLong = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
			buffer.put(v);
			buffer.flip();//need flip 
			return buffer.getLong();
		};
		Function<Long, UUID> longToUUID = v -> {
			return new UUID(v, 0L);
		};
		result.putAll(
				makePrimitiveMappersFor(Long.class,
						/** Boolean */v -> 0L < v, v -> v ? 1L : 0L,
						/** Bytes   */longToBytes, bytesToLong,
						/** Chars   */v -> v.toString().toCharArray(), chars -> Long.parseLong(chars.toString()),
						/** Short   */v -> v.shortValue(), v -> v.longValue(),
						/** Integer */v -> v.intValue(), v -> v.longValue(),
						/** Long    */Function.identity(), Function.identity(),
						/** Double  */v -> v.doubleValue(), v -> v.longValue(),
						/** Float   */v -> v.floatValue(), v -> v.longValue(),
						/** String  */Objects::toString, Long::parseLong,
						/** UUID    */longToUUID, UUID::getMostSignificantBits
				)
		);

		// ------------------ FROM: Integer ----------------
		Function<Integer, byte[]> integerToBytes = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
			buffer.putInt(v);
			return buffer.array();
		};
		Function<byte[], Integer> bytesToInteger = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
			buffer.put(v);
			buffer.flip();//need flip 
			return buffer.getInt();
		};
		Function<Integer, UUID> integerToUUID = v -> {
			long mostSigBit = v.longValue();
			return new UUID(mostSigBit, 0L);
		};
		Function<UUID, Integer> UUIDToInteger = v -> {
			Long mostSigBit = v.getMostSignificantBits();
			return mostSigBit.intValue();
		};
		result.putAll(
				makePrimitiveMappersFor(Integer.class,
						/** Boolean */v -> 0 < v, v -> v ? 1 : 0,
						/** Bytes   */integerToBytes, bytesToInteger,
						/** Chars   */v -> v.toString().toCharArray(), chars -> Integer.parseInt(chars.toString()),
						/** Short   */v -> v.shortValue(), v -> v.intValue(),
						/** Integer */Function.identity(), Function.identity(),
						/** Long    */v -> v.longValue(), v -> v.intValue(),
						/** Double  */v -> v.doubleValue(), v -> v.intValue(),
						/** Float   */v -> v.floatValue(), v -> v.intValue(),
						/** String  */Objects::toString, Integer::parseInt,
						/** UUID    */integerToUUID, UUIDToInteger
				)
		);

		// ------------------ FROM: Short ----------------
		Function<Short, byte[]> shortToBytes = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
			buffer.putShort(v);
			return buffer.array();
		};
		Function<byte[], Short> bytesToShort = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
			buffer.put(v);
			buffer.flip();//need flip 
			return buffer.getShort();
		};
		Function<Short, UUID> shortToUUID = v -> {
			long mostSigBit = v.longValue();
			return new UUID(mostSigBit, 0L);
		};
		Function<UUID, Short> UUIDToShort = v -> {
			Long mostSigBit = v.getMostSignificantBits();
			return mostSigBit.shortValue();
		};
		result.putAll(
				makePrimitiveMappersFor(Short.class,
						/** Boolean */v -> 0 < v, v -> (short) (v ? 1 : 0),
						/** bytes   */shortToBytes, bytesToShort,
						/** chars   */v -> v.toString().toCharArray(), v -> Short.parseShort(v.toString()),
						/** short   */Function.identity(), Function.identity(),
						/** Integer */v -> v.intValue(), v -> v.shortValue(),
						/** Long    */v -> v.longValue(), v -> v.shortValue(),
						/** Double  */v -> v.doubleValue(), v -> v.shortValue(),
						/** Float   */v -> v.floatValue(), v -> v.shortValue(),
						/** String  */Objects::toString, Short::parseShort,
						/** UUID    */shortToUUID, UUIDToShort
				)
		);

		// ------------------ FROM: Double ----------------
		Function<Double, byte[]> doubleToBytes = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
			buffer.putDouble(v);
			return buffer.array();
		};
		Function<byte[], Double> bytesToDouble = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
			buffer.put(v);
			buffer.flip();//need flip 
			return buffer.getDouble();
		};
		Function<Double, UUID> doubleToUUID = v -> {
			long mostSigBit = v.longValue();
			return new UUID(mostSigBit, 0L);
		};
		Function<UUID, Double> UUIDToDouble = v -> {
			Long mostSigBit = v.getMostSignificantBits();
			return mostSigBit.doubleValue();
		};
		result.putAll(
				makePrimitiveMappersFor(Double.class,
						/** Boolean */v -> 0.0 < v, v -> (v ? 1.0 : 0.0),
						/** bytes   */doubleToBytes, bytesToDouble,
						/** chars   */v -> v.toString().toCharArray(), v -> Double.parseDouble(v.toString()),
						/** short   */v -> v.shortValue(), v -> v.doubleValue(),
						/** Integer */v -> v.intValue(), v -> v.doubleValue(),
						/** Long    */v -> v.longValue(), v -> v.doubleValue(),
						/** Double  */Function.identity(), Function.identity(),
						/** Float   */v -> v.floatValue(), v -> v.doubleValue(),
						/** String  */Objects::toString, Double::parseDouble,
						/** UUID    */doubleToUUID, UUIDToDouble
				)
		);

		// ------------------ FROM: Float ----------------
		Function<Float, byte[]> floatToBytes = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
			buffer.putFloat(v);
			return buffer.array();
		};
		Function<byte[], Float> bytesToFloat = v -> {
			ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
			buffer.put(v);
			buffer.flip();//need flip 
			return buffer.getFloat();
		};

		Function<Float, UUID> floatToUUID = v -> {
			long mostSigBit = v.longValue();
			return new UUID(mostSigBit, 0L);
		};
		Function<UUID, Float> UUIDToFloat = v -> {
			Long mostSigBit = v.getMostSignificantBits();
			return mostSigBit.floatValue();
		};
		result.putAll(
				makePrimitiveMappersFor(Float.class,
						/** Boolean */v -> 0.0 < v, v -> (float) (v ? 1.0 : 0.0),
						/** bytes   */floatToBytes, bytesToFloat,
						/** chars   */v -> v.toString().toCharArray(), v -> Float.parseFloat(v.toString()),
						/** short   */v -> v.shortValue(), v -> v.floatValue(),
						/** Integer */v -> v.intValue(), v -> v.floatValue(),
						/** Long    */v -> v.longValue(), v -> v.floatValue(),
						/** Double  */v -> v.doubleValue(), v -> v.floatValue(),
						/** Float   */Function.identity(), Function.identity(),
						/** String  */Objects::toString, Float::parseFloat,
						/** UUID    */floatToUUID, UUIDToFloat
				)
		);

		// ------------------ FROM: String ----------------
		result.putAll(
				makePrimitiveMappersFor(String.class,
						/** Boolean */Boolean::parseBoolean, Objects::toString,
						/** bytes   */String::getBytes, Object::toString,
						/** chars   */String::toCharArray, Object::toString,
						/** short   */Short::parseShort, Object::toString,
						/** Integer */Integer::parseInt, Object::toString,
						/** Long    */Long::parseLong, Object::toString,
						/** Double  */Double::parseDouble, Object::toString,
						/** Float   */null, Object::toString,
						/** String  */Function.identity(), Function.identity(),
						/** UUID    */UUID::fromString, Object::toString
				)
		);

		// ------------------ FROM: Chars ----------------
		Function<char[], byte[]> charsToBytes = v -> {
			CharBuffer charBuffer = CharBuffer.wrap(v);
			ByteBuffer byteBuffer = Charset.forName(StandardCharsets.UTF_8.name()).encode(charBuffer);
			byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
					byteBuffer.position(), byteBuffer.limit());
			return bytes;
		};
		Function<byte[], char[]> bytesToChars = v -> {
			ByteBuffer byteBuffer = ByteBuffer.wrap(v);
			CharBuffer charBuffer = Charset.forName(StandardCharsets.UTF_8.name()).decode(byteBuffer);
			char[] chars = Arrays.copyOfRange(charBuffer.array(),
					charBuffer.position(), charBuffer.limit());
			return chars;
		};
		Function<UUID, char[]> UUIDToChars = v -> {
			ByteBuffer byteBuffer = ByteBuffer.wrap(v.toString().getBytes());
			CharBuffer charBuffer = Charset.forName(StandardCharsets.UTF_8.name()).decode(byteBuffer);
			char[] chars = Arrays.copyOfRange(charBuffer.array(),
					charBuffer.position(), charBuffer.limit());
			return chars;
		};
		Function<char[], UUID> charsToUUID = v -> {
			return UUID.fromString(v.toString());
		};
		result.putAll(
				makePrimitiveMappersFor(char[].class,
						/** Boolean */v -> Boolean.parseBoolean(v.toString()), v -> v.toString().toCharArray(),
						/** bytes   */charsToBytes, bytesToChars,
						/** chars   */Function.identity(), Function.identity(),
						/** short   */v -> Short.parseShort(v.toString()), v -> v.toString().toCharArray(),
						/** Integer */v -> Integer.parseInt(v.toString()), v -> v.toString().toCharArray(),
						/** Long    */v -> Long.parseLong(v.toString()), v -> v.toString().toCharArray(),
						/** Double  */v -> Double.parseDouble(v.toString()), v -> v.toString().toCharArray(),
						/** Float   */v -> Float.parseFloat(v.toString()), v -> v.toString().toCharArray(),
						/** String  */Objects::toString, String::toCharArray,
						/** UUID    */charsToUUID, UUIDToChars
				)
		);

		// ------------------ FROM: Boolean ----------------
		Function<Boolean, byte[]> booleanToBytes = v -> {
			byte[] buffer = new byte[1];
			buffer[0] = (byte) (v ? 1 : 0);
			return buffer;
		};
		Function<byte[], Boolean> bytesToBoolean = v -> {
			return v[0] == (byte) 1;
		};

		Function<Boolean, UUID> booleanToUUID = v -> {
			long mostSigBit = v ? 0L : 1L;
			return new UUID(mostSigBit, 0L);
		};

		Function<UUID, Boolean> UUIDToBoolean = v -> {
			long mostSigBit = v.getMostSignificantBits();
			long leastSgBit = v.getLeastSignificantBits();
			return mostSigBit == leastSgBit && mostSigBit == 1L;
		};

		result.putAll(
				makePrimitiveMappersFor(Boolean.class,
						/** Boolean */Function.identity(), Function.identity(),
						/** bytes   */booleanToBytes, bytesToBoolean,
						/** chars   */v -> v.toString().toCharArray(), v -> Boolean.parseBoolean(v.toString()),
						/** short   */v -> (short) (v ? 1 : 0), v -> 0 < v,
						/** Integer */v -> v ? 1 : 0, v -> 0 < v,
						/** Long    */v -> v ? 1L : 0L, v -> 0L < v,
						/** Double  */v -> v ? 1.0 : 0.0, v -> 0.0 < v,
						/** Float   */v -> (float) (v ? 1.0 : 0.0), v -> (float) 0.0 < v,
						/** String  */Objects::toString, Boolean::parseBoolean,
						/** UUID    */booleanToUUID, UUIDToBoolean
				)
		);

		// ------------------ FROM: Bytes ----------------
		Function<UUID, byte[]> UUIDToBytes = uuid -> {
			ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
			bb.putLong(uuid.getMostSignificantBits());
			bb.putLong(uuid.getLeastSignificantBits());

			return bb.array();
		};

		Function<byte[], UUID> bytesToUUID = bytes -> {
			ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
			Long high = byteBuffer.getLong();
			Long low = byteBuffer.getLong();

			return new UUID(high, low);
		};
		result.putAll(
				makePrimitiveMappersFor(byte[].class,
						/** Boolean */bytesToBoolean, booleanToBytes,
						/** bytes   */Function.identity(), Function.identity(),
						/** chars   */bytesToChars, charsToBytes,
						/** short   */bytesToShort, shortToBytes,
						/** Integer */bytesToInteger, integerToBytes,
						/** Long    */bytesToLong, longToBytes,
						/** Double  */bytesToDouble, doubleToBytes,
						/** Float   */bytesToFloat, floatToBytes,
						/** String  */Objects::toString, String::getBytes,
						/** UUID    */bytesToUUID, UUIDToBytes
				)
		);

		// ------------------ FROM: UUID ----------------
		result.putAll(
				makePrimitiveMappersFor(UUID.class,
						/** Boolean */UUIDToBoolean, booleanToUUID,
						/** bytes   */UUIDToBytes, bytesToUUID,
						/** chars   */UUIDToChars, charsToUUID,
						/** short   */UUIDToShort, shortToUUID,
						/** Integer */UUIDToInteger, integerToUUID,
						/** Long    */UUID::getMostSignificantBits, longToUUID,
						/** Double  */UUIDToDouble, doubleToUUID,
						/** Float   */UUIDToFloat, floatToUUID,
						/** String  */Object::toString, UUID::fromString,
						/** UUID    */Function.identity(), Function.identity()
				)
		);

		return result;
	}

	// boolean , byte , char, short , int , long , float and double
	private static <T> Map<String, Adapter> makePrimitiveMappersFor(
			Class<T> sourceType,
			Function<T, Boolean> toBoolean, Function<Boolean, T> fromBoolean,
			Function<T, byte[]> toBytes, Function<byte[], T> fromBytes,
			Function<T, char[]> toChars, Function<char[], T> fromChars,
			Function<T, Short> toShort, Function<Short, T> fromShort,
			Function<T, Integer> toInteger, Function<Integer, T> fromInteger,
			Function<T, Long> toLong, Function<Long, T> fromLong,
			Function<T, Double> toDouble, Function<Double, T> fromDouble,
			Function<T, Float> toFloat, Function<Float, T> fromFloat,
			Function<T, String> toString, Function<String, T> fromString,
			Function<T, UUID> toUUID, Function<UUID, T> fromUUID
	) {
		Map<String, Adapter> result = new HashMap<>();
		result.put(
				getKey(sourceType, Boolean.class),
				makeAdapter(fromBoolean, toBoolean)
		);
		result.put(
				getKey(sourceType, byte[].class),
				makeAdapter(fromBytes, toBytes)
		);
		result.put(
				getKey(sourceType, char[].class),
				makeAdapter(fromChars, toChars)
		);
		result.put(
				getKey(sourceType, Short.class),
				makeAdapter(fromShort, toShort)
		);

		result.put(
				getKey(sourceType, Integer.class),
				makeAdapter(fromInteger, toInteger)
		);

		result.put(
				getKey(sourceType, Long.class),
				makeAdapter(fromLong, toLong)
		);

		result.put(
				getKey(sourceType, Double.class),
				makeAdapter(fromDouble, toDouble)
		);

		result.put(
				getKey(sourceType, Float.class),
				makeAdapter(fromFloat, toFloat)
		);

		result.put(
				getKey(sourceType, String.class),
				makeAdapter(fromString, toString)
		);

		result.put(
				getKey(sourceType, UUID.class),
				makeAdapter(fromUUID, toUUID)
		);
		return result;
	}


}
