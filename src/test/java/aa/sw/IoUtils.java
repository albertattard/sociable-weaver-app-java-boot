package aa.sw;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static java.util.Objects.requireNonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IoUtils {

    public static void copyDirectory(final Path source, final Path destination) {
        requireNonNull(source);
        requireNonNull(destination);

        uncheckedIo(() -> Files.walk(source)
                .forEach(a -> {
                    Path b = destination.resolve(source.relativize(a));
                    uncheckedIo(() -> Files.copy(a, b));
                })
        );
    }

    public static void emptyDirectory(final Path directory) {
        requireNonNull(directory);

        uncheckedIo(() -> {
            if (Files.isDirectory(directory)) {
                Files.walk(directory)
                        .sorted(Comparator.reverseOrder())
                        .forEach(IoUtils::delete);
            }

            // Files.createDirectories(directory);
        });
    }

    public static void delete(final Path file) {
        requireNonNull(file);

        uncheckedIo(() -> Files.delete(file));
    }

    public static void uncheckedIo(final IoRunnable runnable) {
        requireNonNull(runnable);

        try {
            runnable.run();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T uncheckedIo(final IoSupplier<T> supplier) {
        requireNonNull(supplier);

        try {
            return supplier.get();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public interface IoSupplier<T> {
        T get() throws IOException;
    }

    public interface IoRunnable {
        void run() throws IOException;
    }
}
