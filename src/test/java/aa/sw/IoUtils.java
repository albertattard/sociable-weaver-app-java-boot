package aa.sw;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static aa.sw.common.UncheckedIo.uncheckedIo;
import static java.util.Objects.requireNonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IoUtils {

    public static void copyDirectory(final Path source, final Path destination) {
        requireNonNull(source);
        requireNonNull(destination);

        ensureParentDirectoryExists(destination);

        uncheckedIo(() -> Files.walk(source)
                .forEach(a -> {
                    final Path b = destination.resolve(source.relativize(a));
                    uncheckedIo(() -> Files.copy(a, b));
                })
        );
    }

    private static void ensureParentDirectoryExists(final Path path) {
        requireNonNull(path);

        final Path parent = path.toAbsolutePath().getParent();
        if (!Files.isDirectory(parent)) {
            uncheckedIo(() -> Files.createDirectories(parent));
        }
    }

    public static void emptyDirectory(final Path directory) {
        requireNonNull(directory);

        uncheckedIo(() -> {
            if (Files.isDirectory(directory)) {
                Files.walk(directory)
                        .sorted(Comparator.reverseOrder())
                        .forEach(IoUtils::delete);
            }
        });
    }

    public static void delete(final Path file) {
        requireNonNull(file);

        uncheckedIo(() -> Files.delete(file));
    }
}
