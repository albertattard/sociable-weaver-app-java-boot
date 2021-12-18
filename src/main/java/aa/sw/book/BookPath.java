package aa.sw.book;


import lombok.Value;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public String toString() {
        return path.toString();
    }
}
