package storages;

import java.util.function.Supplier;

public interface ISetKeyGenerator<T> {

	void setKeyGenerator(Supplier<T> value);

}
