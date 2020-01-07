package vcollections.keygenerators;

import java.util.function.Supplier;
public interface IAccessKeyGenerator<T> {

	void setKeyGenerator(IKeyGenerator<T> value);

	IKeyGenerator<T> getKeyGenerator();
}
