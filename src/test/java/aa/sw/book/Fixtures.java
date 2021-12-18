package aa.sw.book;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

class Fixtures {

    static final Path BOOK_DIRECTORY = Path.of("src/test/resources/fixtures/books");

    static final Path PROLOGUE_FILE = Path.of("00-prologue.json");

    static final ChapterPath PROLOGUE_CHAPTER_PATH = ChapterPath.of(BOOK_DIRECTORY, PROLOGUE_FILE);

    static final Book BOOK = Book.builder()
            .title("Programming")
            .description("A book about programming")
            .chapter("Prologue", "The prologue", PROLOGUE_FILE.toString())
            .chapter("Hello World", "Automation", "01-hello-world.json")
            .chapter("Broken Links", "Test Driven Development", "02-broken-links.json")
            .bookPath(BOOK_DIRECTORY.resolve("book.json"))
            .build();

    static final Chapter.Entry PROLOGUE_ENTRY_1 = Chapter.Entry.builder()
            .type("chapter")
            .id(UUID.fromString("3a50daae-ab81-426f-a118-b505e7eecb49"))
            .parameters(List.of("Prologue"))
            .build();

    static final Chapter.Entry PROLOGUE_ENTRY_2 = Chapter.Entry.builder()
            .type("markdown")
            .id(UUID.fromString("483214f8-fc66-4a3a-b8dc-26401ac6a608"))
            .parameters(List.of("We make mistakes, and we make more mistakes, and some more, and that's how we learn."))
            .build();

    static final Chapter PROLOGUE = Chapter.builder()
            .entry(PROLOGUE_ENTRY_1)
            .entry(PROLOGUE_ENTRY_2)
            .build();

    static Path resolve(final String name) {
        return BOOK_DIRECTORY.resolve(name);
    }

    static File prologueFile() {
        return prologueFile(BOOK_DIRECTORY);
    }

    static File prologueFile(final Path bookDirectory) {
        return bookDirectory.resolve(PROLOGUE_FILE).toFile();
    }
}
