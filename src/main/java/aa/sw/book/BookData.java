package aa.sw.book;

import aa.sw.common.CustomPrettyPrinter;
import aa.sw.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

@Service
public class BookData {

    private final ObjectMapper reader;
    private final ObjectWriter writer;

    public BookData(final ObjectMapper mapper) {
        requireNonNull(mapper);

        this.reader = mapper;
        this.writer = CustomPrettyPrinter.of(mapper);
    }

    public Result<Book> readBook(final Path path) {
        return read(path, Book.class)
                .then(book -> book.withBookPath(path));
    }

    public Result<Chapter> readChapter(final Path path) {
        return read(path, Chapter.class);
    }

    public Result<Chapter> writeChapter(final Path path, final Chapter chapter) {
        return write(path, Chapter.class, chapter);
    }

    private <T> Result<T> read(final Path path, final Class<T> type) {
        return Result.of(() -> reader.readValue(path.toFile(), type));
    }

    private <T> Result<T> write(final Path path, final Class<T> type, final T object) {
        return Result.of(() -> {
            writer.writeValue(path.toFile(), object);
            return reader.readValue(path.toFile(), type);
        });
    }
}
