package aa.sw.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {

    private final T value;
    private final Exception error;

    public static <E> Result<E> of(final ResultSupplier<E> supplier) {
        try {
            return value(supplier.get());
        } catch (final Exception e) {
            return error(e);
        }
    }

    public static <E> Result<E> error(final Exception error) {
        requireNonNull(error);

        return new Result<>(null, error);
    }

    public static <E> Result<E> value(final E value) {
        requireNonNull(value);

        return new Result<>(value, null);
    }

    public <V> V map(final Function<T, V> valueMapper, final Function<Exception, V> errorMapper) {
        requireNonNull(valueMapper);
        requireNonNull(errorMapper);

        return error == null
                ? valueMapper.apply(value)
                : errorMapper.apply(error);
    }

    public <V> Result<V> then(final ResultFunction<T, V> mapper) {
        requireNonNull(mapper);

        return error == null
                ? Result.of(() -> mapper.apply(value))
                : Result.error(error);
    }

    public <V> Result<V> flatThen(final Function<T, Result<V>> mapper) {
        requireNonNull(mapper);

        return error == null
                ? mapper.apply(value)
                : Result.error(error);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) return false;

        final Result<?> other = (Result<?>) object;
        return Objects.equals(value, other.value) && Objects.equals(error, other.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, error);
    }

    @Override
    public String toString() {
        return error == null
                ? String.valueOf(value)
                : String.valueOf(error);
    }

    @FunctionalInterface
    public interface ResultSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ResultFunction<V, R> {
        R apply(V value) throws Exception;
    }
}
