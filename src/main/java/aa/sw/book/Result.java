package aa.sw.book;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/* TODO: Move this class into a more generic package */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {

    private final T value;
    private final Throwable error;

    public static <E> Result<E> of(final ResultSupplier<E> supplier) {
        try {
            return value(supplier.get());
        } catch (final Throwable e) {
            return error(e);
        }
    }

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

    @Override
    public boolean equals(final Object object) {
        if (this == object) { return true; }
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

    public interface ResultSupplier<T> {
        T get() throws Throwable;
    }
}
