package aa.sw.book;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
        return service.readChapter(ChapterPath.of(bookPath, chapterPath))
                .map(ResponseEntity::ok, BookController::createReadChapterErrorResponse);
    }

    @PostMapping("/entry")
    public ResponseEntity<?> createEntry(@RequestParam("bookPath") final Path bookPath,
                                         @RequestParam("chapterPath") final Path chapterPath,
                                         @RequestBody final CreateEntry entry) {
        return service.createEntry(ChapterPath.of(bookPath, chapterPath), entry)
                .map(ResponseEntity::ok, BookController::createSaveEntryErrorResponse);
    }

    @PutMapping("/entry")
    public ResponseEntity<?> saveEntry(@RequestParam("bookPath") final Path bookPath,
                                       @RequestParam("chapterPath") final Path chapterPath,
                                       @RequestBody final Chapter.Entry entry) {
        return service.saveEntry(ChapterPath.of(bookPath, chapterPath), entry)
                .map(ResponseEntity::ok, BookController::createSaveEntryErrorResponse);
    }

    private static ResponseEntity<?> createOpenErrorResponse(final Throwable e) {
        final String message = e instanceof FileNotFoundException
                ? "Book not found"
                : formatUnexpectedError(e);

        return createUnprocessableEntityResponse(message);
    }

    private static ResponseEntity<?> createReadChapterErrorResponse(final Throwable e) {
        final String message = e instanceof FileNotFoundException
                ? "Chapter not found"
                : formatUnexpectedError(e);

        return createUnprocessableEntityResponse(message);
    }

    private static ResponseEntity<?> createSaveEntryErrorResponse(final Throwable e) {
        final String message;
        if (e instanceof FileNotFoundException) {
            message = "Chapter not found";
        } else if (e instanceof EntryNotFoundException) {
            message = "Entry not found in chapter";
        } else {
            message = formatUnexpectedError(e);
        }

        return createUnprocessableEntityResponse(message);
    }

    private static ResponseEntity<Map<String, String>> createUnprocessableEntityResponse(final String message) {
        requireNonNull(message);

        return ResponseEntity.unprocessableEntity()
                .body(Map.of("message", message));
    }

    private static String formatUnexpectedError(final Throwable e) {
        return String.format("Encountered an unexpected error (%s)", e);
    }
}
