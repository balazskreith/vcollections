package vcollections.storages.query;

import java.util.List;
import java.util.function.Function;

public interface ListQuery<T, V> extends Function<T, List<V>> {

}
