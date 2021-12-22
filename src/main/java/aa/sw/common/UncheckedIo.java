package aa.sw.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.UncheckedIOException;

import static java.util.Objects.requireNonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UncheckedIo {

    public static void quietIo(final IoRunnable runnable) {
        requireNonNull(runnable);

        try {
            runnable.run();
        } catch (final IOException e) { /* Suppress IO errors */ }
    }

    public static void uncheckedIo(final IoRunnable runnable) {
        uncheckedIo(null, runnable);
    }

    public static void uncheckedIo(final String message, final IoRunnable runnable) {
        requireNonNull(runnable);

        try {
            runnable.run();
        } catch (final IOException e) {
            throw new UncheckedIOException(message, e);
        }
    }

    public static <T> T uncheckedIo(final IoSupplier<T> supplier) {
        return uncheckedIo(null, supplier);
    }

    public static <T> T uncheckedIo(final String message, final IoSupplier<T> supplier) {
        requireNonNull(supplier);

        try {
            return supplier.get();
        } catch (final IOException e) {
            throw new UncheckedIOException(message, e);
        }
    }

    public interface IoSupplier<T> {
        T get() throws IOException;
    }

    public interface IoRunnable {
        void run() throws IOException;
    }
}
