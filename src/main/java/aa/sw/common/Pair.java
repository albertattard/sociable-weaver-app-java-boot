package aa.sw.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.UncheckedIOException;

import static java.util.Objects.requireNonNull;

@Value
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Pair<L, R> {

    L left;
    R right;

    public static <L, R> Pair<L, R> of(final L left, final R right) {
        requireNonNull(left);
        requireNonNull(right);

        return new Pair<>(left, right);
    }

    public Pair<L, R> with(final Consumer<L, R> consumer) {
        requireNonNull(consumer);

        try {
            consumer.accept(left, right);
            return this;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface Consumer<L, R> {
        void accept(L left, R right) throws Exception;
    }
}
