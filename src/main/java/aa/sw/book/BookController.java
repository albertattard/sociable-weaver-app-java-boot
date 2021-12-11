package aa.sw.book;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/book")
@AllArgsConstructor
public class BookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);

    private final BookService service;

    @GetMapping("/open")
    public ResponseEntity<?> open(@RequestParam("bookPath") final Path bookPath) {
        return service.openBook(bookPath)
                .map(ResponseEntity::ok, this::createOpenErrorResponse);
    }

    @GetMapping("/read-chapter")
    public ResponseEntity<?> readChapter(@RequestParam("bookPath") final Path bookPath,
                                         @RequestParam("chapterPath") final Path chapterPath) {
        return service.readChapter(bookPath, chapterPath)
                .map(ResponseEntity::ok, this::createReadChapterErrorResponse);
    }

    private ResponseEntity<?> createOpenErrorResponse(final Throwable e) {
        LOGGER.error("Failed to open book", e);

        final String message = e instanceof FileNotFoundException
                ? "Book not found"
                : "Encountered an unexpected error";

        return createUnprocessableEntityResponse(message);
    }

    private ResponseEntity<?> createReadChapterErrorResponse(final Throwable e) {
        LOGGER.error("Failed to read chapter", e);

        final String message = e instanceof FileNotFoundException
                ? "Chapter not found"
                : "Encountered an unexpected error";

        return createUnprocessableEntityResponse(message);
    }

    private ResponseEntity<Map<String, String>> createUnprocessableEntityResponse(final String message) {
        requireNonNull(message);

        return ResponseEntity.unprocessableEntity()
                .body(Map.of("message", message));
    }
}
