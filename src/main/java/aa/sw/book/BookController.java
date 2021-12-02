package aa.sw.book;

import lombok.AllArgsConstructor;
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

    private final BookService service;

    @GetMapping("/open")
    public ResponseEntity<?> open(@RequestParam("path") final Path path) {
        return service.openBook(path)
                .map(ResponseEntity::ok, this::createOpenErrorResponse);
    }

    @GetMapping("/read-chapter")
    public ResponseEntity<?> readChapter(@RequestParam("path") final Path path) {
        return service.readChapter(path)
                .map(ResponseEntity::ok, this::createReadChapterErrorResponse);
    }

    private ResponseEntity<?> createOpenErrorResponse(final Throwable e) {
        final String message = e instanceof FileNotFoundException
                ? "Folder not found"
                : "Encountered an unexpected error";

        return createUnprocessableEntityResponse(message);
    }

    private ResponseEntity<?> createReadChapterErrorResponse(final Throwable e) {
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
