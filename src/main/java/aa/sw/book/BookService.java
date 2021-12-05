package aa.sw.book;

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

    public Result<Book> openBook(final Path path) {
        requireNonNull(path);

        final Path file = Files.isDirectory(path)
                ? path.resolve("book.json")
                : path;

        return Result.of(() -> mapper.readValue(file.toFile(), Book.class));
    }

    public Result<Chapter> readChapter(final Path path) {
        requireNonNull(path);

        /* TODO: Read the chapter from the given path */
        return Result.of(() -> mapper.readValue(getClass().getResource("/fixtures/00-prologue.json"), Chapter.class));
    }
}
