package aa.sw.book;

import aa.sw.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
public class BookService {

    private final ObjectMapper mapper;

    public Result<Book> openBook(final Path bookPath) {
        requireNonNull(bookPath);

        return Result.of(() -> {
            final Path file = Files.isDirectory(bookPath)
                    ? bookPath.resolve("book.json")
                    : bookPath;
            return mapper.readValue(file.toFile(), Book.class);
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

            return mapper.readValue(file.toFile(), Chapter.class);
        });
    }

    public Result<Chapter.Entry> saveEntry(final Path bookPath, final Path chapterPath, final Chapter.Entry entry) {
        requireNonNull(bookPath);
        requireNonNull(chapterPath);
        requireNonNull(entry);

        return Result.error(new UnsupportedOperationException("Not yet implemented"));
    }
}
