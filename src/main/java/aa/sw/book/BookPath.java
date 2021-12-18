package aa.sw.book;


import lombok.Value;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Value
public class BookPath {

    Path path;

    public static BookPath of(final Path path) {
        requireNonNull(path);

        final Path file = Files.isDirectory(path)
                ? path.resolve("book.json")
                : path;

        return new BookPath(file);
    }

    public File getFile() {
        return path.toFile();
    }

    public Path getDirectory() {
        /* When given just a single name and not a path, like `BookPath.of("some-file.json")`,
            then the `getParent()` will return `null`.  In this case, we need to switch this to
            an absolute path and then get the parent. */
        return Optional.ofNullable(path.getParent())
                .orElse(path.toAbsolutePath().getParent());
    }

    public String toString() {
        return path.toString();
    }
}
