package com.wobserver.vcollections.storages;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class PrimitiveTypesMapperFactory<TS, TR> {

	private static Map<String, IMapper> mappers = makeMappers();
	private static Set<String> primitiveTypes = Set.of(
			Boolean.class.getName(),
			String.class.getName(),
			byte[].class.getName(),
			char[].class.getName(),
			Long.class.getName(),
			Short.class.getName(),
			Integer.class.getName(),
			Float.class.getName(),
			Double.class.getName()
	);

	public static<S, R> IMapper<S, R> makeMapper(Function<S, R>  encoder, Function<R, S> decoder) {
		return new IMapper<S, R>() {
			private final Function<S, R> encode = encoder;
			private final Function<R, S> decode = decoder;
			@Override
			public R encode(S value) {
				return this.encode.apply(value);
			}

			@Override
			public S decode(R value) {
				return this.decode.apply(value);
			}
		};
	}

	public static<T> boolean isPrimitiveType(Class<T> type) {
		return isPrimitiveType(type.getName());
	}

	public static boolean isPrimitiveType(String typeName) {
		return primitiveTypes.contains(typeName);
	}
	
	
	public static <TSS, TRS> IMapper<TSS, TRS> make(Class<TSS> sourceType, Class<TRS> resutType) {
		return new PrimitiveTypesMapperFactory<TSS, TRS>(sourceType, resutType).make();
	}
	
	private Class<TS> sourceType;
	private Class<TR> resultType;
	
	public PrimitiveTypesMapperFactory(Class<TS> sourceType, Class<TR> resultType) {
		this.sourceType = sourceType;
		this.resultType = resultType;
		mappers.putAll(this.makeMappers());
	}
	
	public IMapper<TS, TR> make() {
		String key = this.getKey(sourceType, resultType);
		IMapper<TS, TR> result = (IMapper<TS, TR>) this.mappers.get(key);
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

	private static Map<String, IMapper> makeMappers() {
		Map<String, IMapper> result = new HashMap<>();

		// ------------------ FROM: String ----------------
		result.putAll(
				makePrimitiveMappersFor(String.class,
						/** Boolean */ Boolean::getBoolean, Objects::toString,
						/** Bytes   */ str -> str.getBytes(), Objects::toString,
						/** Chars   */ str -> str.toCharArray(), chars -> new String(chars),
						/** Short   */ Short::parseShort, Objects::toString,
						/** Integer */ Integer::parseInt, Objects::toString,
						/** Long    */ Long::parseLong, Objects::toString,
						/** Double  */ Double::parseDouble, Objects::toString,
						/** Float   */ Float::parseFloat, Objects::toString,
						/** String  */ Function.identity(), Function.identity()
				)
		);

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
		result.putAll(
				makePrimitiveMappersFor(Long.class,
						/** Boolean */ v -> 0L < v, v -> v ? 1L : 0L,
						/** Bytes   */ longToBytes, bytesToLong,
						/** Chars   */ v -> v.toString().toCharArray(), chars -> Long.parseLong(chars.toString()),
						/** Short   */ v -> v.shortValue(), v -> v.longValue(),
						/** Integer */ v -> v.intValue(), v -> v.longValue(),
						/** Long    */ Function.identity(), Function.identity(),
						/** Double  */ v -> v.doubleValue(), v -> v.longValue(),
						/** Float   */ v -> v.floatValue(), v -> v.longValue(),
						/** String  */ Objects::toString, Long::parseLong
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
		result.putAll(
				makePrimitiveMappersFor(Integer.class,
						/** Boolean */ v -> 0 < v, v -> v ? 1 : 0,
						/** Bytes   */ integerToBytes, bytesToInteger,
						/** Chars   */ v -> v.toString().toCharArray(), chars -> Integer.parseInt(chars.toString()),
						/** Short   */ v -> v.shortValue(), v -> v.intValue(),
						/** Integer */ Function.identity(), Function.identity(),
						/** Long    */ v -> v.longValue(), v -> v.intValue(),
						/** Double  */ v -> v.doubleValue(), v -> v.intValue(),
						/** Float   */ v -> v.floatValue(), v -> v.intValue(),
						/** String  */ Objects::toString, Integer::parseInt
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
		result.putAll(
				makePrimitiveMappersFor(Short.class,
						/** Boolean */ v -> 0 < v, v -> (short) (v ? 1 : 0),
						/** bytes   */ shortToBytes, bytesToShort,
						/** chars   */ v -> v.toString().toCharArray(), v -> Short.parseShort(v.toString()),
						/** short   */ Function.identity(),Function.identity(),
						/** Integer */ v -> v.intValue(), v -> v.shortValue(),
						/** Long    */ v -> v.longValue(), v -> v.shortValue(),
						/** Double  */ v -> v.doubleValue(), v -> v.shortValue(),
						/** Float   */ v-> v.floatValue(), v -> v.shortValue(),
						/** String  */Objects::toString, Short::parseShort
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
		result.putAll(
				makePrimitiveMappersFor(Double.class,
						/** Boolean */ v -> 0.0 < v, v -> (v ? 1.0 : 0.0),
						/** bytes   */ doubleToBytes, bytesToDouble,
						/** chars   */ v -> v.toString().toCharArray(), v -> Double.parseDouble(v.toString()),
						/** short   */ v -> v.shortValue(), v-> v.doubleValue(),
						/** Integer */ v -> v.intValue(), v -> v.doubleValue(),
						/** Long    */ v -> v.longValue(), v -> v.doubleValue(),
						/** Double  */ Function.identity(),Function.identity(),
						/** Float   */ v-> v.floatValue(), v -> v.doubleValue(),
						/** String  */Objects::toString, Double::parseDouble
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
		result.putAll(
				makePrimitiveMappersFor(Float.class,
						/** Boolean */ v -> 0.0 < v, v -> (float)(v ? 1.0 : 0.0),
						/** bytes   */ floatToBytes, bytesToFloat,
						/** chars   */ v -> v.toString().toCharArray(), v -> Float.parseFloat(v.toString()),
						/** short   */ v -> v.shortValue(), v-> v.floatValue(),
						/** Integer */ v -> v.intValue(), v -> v.floatValue(),
						/** Long    */ v -> v.longValue(), v -> v.floatValue(),
						/** Double  */ v-> v.doubleValue(), v -> v.floatValue(),
						/** Float   */ Function.identity(),Function.identity(),
						/** String  */Objects::toString, Float::parseFloat
				)
		);

		// ------------------ FROM: String ----------------
		result.putAll(
				makePrimitiveMappersFor(String.class,
						/** Boolean */ Boolean::parseBoolean, Objects::toString,
						/** bytes   */ String::getBytes, Object::toString,
						/** chars   */ String::toCharArray, Object::toString,
						/** short   */ Short::parseShort, Object::toString,
						/** Integer */ Integer::parseInt, Object::toString,
						/** Long    */ Long::parseLong, Object::toString,
						/** Double  */ Double::parseDouble, Object::toString,
						/** Float   */ null,Object::toString,
						/** String  */Function.identity(),Function.identity()
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
		result.putAll(
				makePrimitiveMappersFor(char[].class,
						/** Boolean */ v -> Boolean.parseBoolean(v.toString()), v -> v.toString().toCharArray(),
						/** bytes   */ charsToBytes, bytesToChars,
						/** chars   */ Function.identity(), Function.identity(),
						/** short   */ v -> Short.parseShort(v.toString()), v -> v.toString().toCharArray(), 
						/** Integer */ v -> Integer.parseInt(v.toString()), v -> v.toString().toCharArray(),
						/** Long    */ v -> Long.parseLong(v.toString()), v -> v.toString().toCharArray(),
						/** Double  */ v -> Double.parseDouble(v.toString()), v -> v.toString().toCharArray(),
						/** Float   */ v -> Float.parseFloat(v.toString()), v -> v.toString().toCharArray(),
						/** String  */ Objects::toString, String::toCharArray
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
		result.putAll(
				makePrimitiveMappersFor(Boolean.class,
						/** Boolean */ Function.identity(), Function.identity(),
						/** bytes   */ booleanToBytes, bytesToBoolean,
						/** chars   */ v -> v.toString().toCharArray(), v -> Boolean.parseBoolean(v.toString()),
						/** short   */ v -> (short) (v ? 1 : 0), v -> 0 < v,
						/** Integer */ v -> v ? 1 : 0, v -> 0 < v,
						/** Long    */ v -> v ? 1L : 0L, v -> 0L < v,
						/** Double  */ v -> v ? 1.0 : 0.0, v -> 0.0 < v,
						/** Float   */ v -> (float)(v ? 1.0 : 0.0), v -> (float)0.0 < v,
						/** String  */Objects::toString, Boolean::parseBoolean
				)
		);

		// ------------------ FROM: Bytes ----------------
		result.putAll(
				makePrimitiveMappersFor(byte[].class,
						/** Boolean */ bytesToBoolean, booleanToBytes,
						/** bytes   */ Function.identity(), Function.identity(),
						/** chars   */ bytesToChars, charsToBytes,
						/** short   */ bytesToShort, shortToBytes,
						/** Integer */ bytesToInteger, integerToBytes,
						/** Long    */ bytesToLong, longToBytes,
						/** Double  */ bytesToDouble, doubleToBytes,
						/** Float   */ bytesToFloat, floatToBytes,
						/** String  */ Objects::toString, String::getBytes
				)
		);
		
		return result;
	}
	
	// boolean , byte , char, short , int , long , float and double
	private static<T> Map<String, IMapper> makePrimitiveMappersFor(
			Class<T> sourceType,
			Function<T, Boolean> toBoolean, Function<Boolean, T> fromBoolean,
			Function<T, byte[]> toBytes, Function<byte[], T> fromBytes,
			Function<T, char[]> toChars, Function<char[], T> fromChars,
			Function<T, Short> toShort, Function<Short, T> fromShort,
			Function<T, Integer> toInteger, Function<Integer, T> fromInteger,
			Function<T, Long> toLong, Function<Long, T> fromLong,
			Function<T, Double> toDouble, Function<Double, T> fromDouble,
			Function<T, Float> toFloat, Function<Float, T> fromFloat,
			Function<T, String> toString, Function<String, T> fromString
			) {
		Map<String, IMapper> result = new HashMap<>();
		result.put(
				getKey(sourceType, Boolean.class),
				makeMapper(fromBoolean, toBoolean)
		);
		result.put(
				getKey(sourceType, byte[].class),
				makeMapper(fromBytes, toBytes)
		);
		result.put(
				getKey(sourceType, char[].class),
				makeMapper(fromChars, toChars)
		);
		result.put(
				getKey(sourceType, Short.class),
				makeMapper(fromShort, toShort)
		);

		result.put(
				getKey(sourceType, Integer.class),
				makeMapper(fromInteger, toInteger)
		);

		result.put(
				getKey(sourceType, Long.class),
				makeMapper(fromLong, toLong)
		);

		result.put(
				getKey(sourceType, Double.class),
				makeMapper(fromDouble, toDouble)
		);

		result.put(
				getKey(sourceType, Float.class),
				makeMapper(fromFloat, toFloat)
		);

		result.put(
				getKey(sourceType, String.class),
				makeMapper(fromString, toString)
		);
		return result;
	}
	
	
}
