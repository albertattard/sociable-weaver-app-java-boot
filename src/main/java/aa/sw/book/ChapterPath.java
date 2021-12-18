package aa.sw.book;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.File;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChapterPath {

    Path path;

    public static ChapterPath of(final Path bookPath, final Path chapterPath) {
        requireNonNull(bookPath);
        requireNonNull(chapterPath);

        return of(BookPath.of(bookPath), chapterPath);
    }

    public static ChapterPath of(final BookPath bookPath, final Path chapterPath) {
        requireNonNull(bookPath);
        requireNonNull(chapterPath);

        final Path path = bookPath.getDirectory().resolve(chapterPath);
        return new ChapterPath(path);
    }

    public File getFile() {
        return path.toFile();
    }

    public String toString() {
        return path.toString();
    }
}
