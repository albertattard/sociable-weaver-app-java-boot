package aa.sw.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
public class BookService {

    private final ObjectMapper mapper;

    public Result<Book> openBook(final Path path) {
        requireNonNull(path);

        return Result.of(() -> mapper.readValue(getClass().getResource("/fixtures/book.json"), Book.class));
    }
}
