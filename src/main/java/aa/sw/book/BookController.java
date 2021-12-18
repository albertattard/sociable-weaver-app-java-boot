package aa.sw.book;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping("/book")
    public ResponseEntity<?> openBook(@RequestParam("bookPath") final Path bookPath) {
        return service.openBook(BookPath.of(bookPath))
                .map(ResponseEntity::ok, BookController::createOpenErrorResponse);
    }

    @GetMapping("/chapter")
    public ResponseEntity<?> readChapter(@RequestParam("bookPath") final Path bookPath,
                                         @RequestParam("chapterPath") final Path chapterPath) {
        return service.readChapter(bookPath, chapterPath)
                .map(ResponseEntity::ok, BookController::createReadChapterErrorResponse);
    }

    @PutMapping("/entry")
    public ResponseEntity<?> saveEntry(@RequestParam("bookPath") final Path bookPath,
                                       @RequestParam("chapterPath") final Path chapterPath,
                                       @RequestBody final Chapter.Entry entry) {
        return service.saveEntry(bookPath, chapterPath, entry)
                .map(ResponseEntity::ok, BookController::createSaveEntryErrorResponse);
    }

    private static ResponseEntity<?> createOpenErrorResponse(final Throwable e) {
        final String message = e instanceof FileNotFoundException
                ? "Book not found"
                : "Encountered an unexpected error";

        return createUnprocessableEntityResponse(message);
    }

    private static ResponseEntity<?> createReadChapterErrorResponse(final Throwable e) {
        final String message = e instanceof FileNotFoundException
                ? "Chapter not found"
                : "Encountered an unexpected error";

        return createUnprocessableEntityResponse(message);
    }

    private static ResponseEntity<?> createSaveEntryErrorResponse(final Throwable e) {
        final String message = e instanceof EntryNotFoundException
                ? "Entry not found in chapter"
                : "Encountered an unexpected error";

        return createUnprocessableEntityResponse(message);
    }

    private static ResponseEntity<Map<String, String>> createUnprocessableEntityResponse(final String message) {
        requireNonNull(message);

        return ResponseEntity.unprocessableEntity()
                .body(Map.of("message", message));
    }
}
