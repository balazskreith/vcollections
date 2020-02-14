package com.wobserver.vcollections.storages;

import java.util.UUID;
import java.util.function.Function;

public class SimpleMapperFactory<TS, TR> {

	public static <TSS, TRS> IMapper<TSS, TRS> make(Class<TSS> sourceType, Class<TRS> resutType) {
		return new SimpleMapperFactory<TSS, TRS>(sourceType, resutType).make();
	}

	Class<TS> sourceType;
	Class<TR> resultType;

	public SimpleMapperFactory(Class<TS> sourceType, Class<TR> resultType) {
		this.sourceType = sourceType;
		this.resultType = resultType;
	}

	public IMapper<TS, TR> make() {

		if (resultType.isAssignableFrom(String.class)) {

			final Function<TS, String> tmpEncoder = Object::toString;
			final Function<String, TS> tmpDecoder;
			if (resultType.isAssignableFrom(Long.class)) {
				tmpDecoder = str -> (TS) Long.valueOf(str);
			} else if (resultType.isAssignableFrom(String.class)) {
				tmpDecoder = str -> (TS) str;
			} else if (resultType.isAssignableFrom(UUID.class)) {
				tmpDecoder = str -> (TS) UUID.fromString(str);
			} else if (resultType.isAssignableFrom(Integer.class)) {
				tmpDecoder = str -> (TS) Integer.valueOf(str);
			} else {
				tmpDecoder = null;
			}
			return new IMapper<TS, TR>() {
				private Function<TS, String> encoder = tmpEncoder;
				private Function<String, TS> decoder = tmpDecoder;
				private String nullValue = new UUID(0L, 0L).toString();

				@Override
				public TR encode(TS value) {
					if (value == null) {
						return (TR) nullValue;
					}
					return (TR) encoder.apply(value);
				}

				@Override
				public TS decode(TR value) {
					if (value.toString().equals(nullValue)) {
						return null;
					}
					return decoder.apply((String) value);
				}
			};
		}

		return null;
	}
}
