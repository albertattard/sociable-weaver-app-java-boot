package aa.sw.open;

import org.springframework.stereotype.Service;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

@Service
public class OpenService {

    public Result<OpenValue> openLocal(final Path path) {
        requireNonNull(path);

        /* TODO: Implement logic */
        final OpenValue book = OpenValue.builder()
                .title("Test Book")
                .description("Test Description")
                .path(path.toString())
                .chapter("Chapter 1", "Test chapter 1", "chapter-1")
                .chapter("Chapter 2", "Test chapter 2", "chapter-2")
                .build();

        return Result.value(book);
    }
}
