package aa.sw.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.function.Consumer;
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

    public boolean isValuePresent() {
        return error == null;
    }

    public T value() {
        return map(Function.identity(), Result::valueNotSet);
    }

    private static <E> E valueNotSet(final Throwable e) {
        throw new IllegalStateException("Value is not set", e);
    }

    public <V> V map(final Function<T, V> valueMapper, final Function<Exception, V> errorMapper) {
        requireNonNull(valueMapper);
        requireNonNull(errorMapper);

        return isValuePresent()
                ? valueMapper.apply(value)
                : errorMapper.apply(error);
    }

    public <V> Result<V> then(final ResultFunction<T, V> mapper) {
        requireNonNull(mapper);

        return isValuePresent()
                ? Result.of(() -> mapper.apply(value))
                : Result.error(error);
    }

    public <V> Result<V> flatThen(final Function<T, Result<V>> mapper) {
        requireNonNull(mapper);

        return isValuePresent()
                ? mapper.apply(value)
                : Result.error(error);
    }

    public Result<T> with(final Consumer<T> consumer) {
        requireNonNull(consumer);
        if (isValuePresent()) {
            consumer.accept(value);
        }
        return this;
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
        return isValuePresent()
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
