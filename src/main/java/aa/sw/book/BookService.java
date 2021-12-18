package aa.sw.book;

import aa.sw.common.CustomPrettyPrinter;
import aa.sw.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

@Service
public class BookService {

    private final ObjectMapper reader;
    private final ObjectWriter writer;

    public BookService(final ObjectMapper mapper) {
        requireNonNull(mapper);

        this.reader = mapper;
        this.writer = CustomPrettyPrinter.of(mapper);
    }

    public Result<Book> openBook(final Path bookPath) {
        requireNonNull(bookPath);

        return Result.of(() -> {
            final Path file = Files.isDirectory(bookPath)
                    ? bookPath.resolve("book.json")
                    : bookPath;
            return reader.readValue(file.toFile(), Book.class)
                    .withBookPath(file);
        });
    }

    public Result<Chapter> readChapter(final Path bookPath, final Path chapterPath) {
        requireNonNull(bookPath);
        requireNonNull(chapterPath);

        return Result.of(() -> {
            final Path directory = Files.isDirectory(bookPath)
                    ? bookPath
                    : bookPath.getParent();

            final Path file = directory.resolve(chapterPath);

            return reader.readValue(file.toFile(), Chapter.class);
        });
    }

    public Result<Chapter.Entry> saveEntry(final Path bookPath, final Path chapterPath, final Chapter.Entry entry) {
        requireNonNull(bookPath);
        requireNonNull(chapterPath);
        requireNonNull(entry);

        return readChapter(bookPath, chapterPath)
                .flatThen(chapter -> {
                    final int index = chapter.indexOf(entry);
                    if (index == -1) {
                        return Result.error(new EntryNotFoundException());
                    }

                    final Chapter updated = chapter
                            .swapEntryAt(index, entry);

                    final Path directory = Files.isDirectory(bookPath)
                            ? bookPath
                            : bookPath.getParent();

                    final Path file = directory.resolve(chapterPath);

                    try {
                        writer.writeValue(file.toFile(), updated);
                    } catch (final IOException e) {
                        return Result.error(e);
                    }

                    return readChapter(bookPath, chapterPath)
                            .then(a -> a.findEntryWithId(entry.getId()))
                            .then(a -> a.map(Chapter.EntryIndex::getEntry)
                                    .orElseThrow(() -> new RuntimeException("")));
                });
    }
}
