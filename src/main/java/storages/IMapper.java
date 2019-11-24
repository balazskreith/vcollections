package storages;

import java.util.function.Function;

public interface IMapper<TS, TR> {

	static <STS, STR> IMapper<STS, STR> make(Function<STS, STR> encoder, Function<STR, STS> decoder) {
		return new IMapper<STS, STR>() {
			@Override
			public STR encode(STS value) {
				return encoder.apply(value);
			}

			@Override
			public STS decode(STR value) {
				return decoder.apply(value);
			}
		};
	}

	TR encode(TS value);

	TS decode(TR value);
}
