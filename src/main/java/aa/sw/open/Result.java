package aa.sw.open;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/* TODO: Move this class into a more generic package */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {

    private final T value;
    private final Throwable error;

    public static <E> Result<E> error(final Throwable error) {
        requireNonNull(error);

        return new Result<>(null, error);
    }

    public static <E> Result<E> value(final E value) {
        requireNonNull(value);

        return new Result<>(value, null);
    }

    public <V> V map(final Function<T, V> valueMapper, final Function<Throwable, V> errorMapper) {
        requireNonNull(valueMapper);
        requireNonNull(errorMapper);

        return error == null
                ? valueMapper.apply(value)
                : errorMapper.apply(error);
    }
}
