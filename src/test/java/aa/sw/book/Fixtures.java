package aa.sw.book;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

class Fixtures {

    static final Path BOOK_DIRECTORY = Path.of("src/test/resources/fixtures/books");

    static final Path PROLOGUE_FILE = Path.of("00-prologue.json");

    static final ChapterPath PROLOGUE_CHAPTER_PATH = ChapterPath.of(BOOK_DIRECTORY, PROLOGUE_FILE);

    static final Entry PROLOGUE_ENTRY_1 = Entry.builder()
            .type("chapter")
            .id(UUID.fromString("3a50daae-ab81-426f-a118-b505e7eecb49"))
            .parameters(List.of("Title:1", "Prologue"))
            .build();

    static final Entry PROLOGUE_ENTRY_2 = Entry.builder()
            .type("markdown")
            .id(UUID.fromString("483214f8-fc66-4a3a-b8dc-26401ac6a608"))
            .parameters(List.of("We make mistakes, and we make more mistakes, and some more, and that's how we learn."))
            .build();

    static final Entry HELLO_WORLD_ENTRY_1 = Entry.builder()
            .type("chapter")
            .id(UUID.fromString("6656a93a-c9ec-4f5b-87bb-6819a3c8a329"))
            .parameters(List.of("Title:1", "Hello World"))
            .build();

    static final Entry HELLO_WORLD_ENTRY_2 = Entry.builder()
            .type("markdown")
            .id(UUID.fromString("c7a233ba-5327-447c-ae63-aa53f79c3791"))
            .parameters(List.of("Automation"))
            .build();

    static final Entry BROKEN_LINKS_ENTRY_1 = Entry.builder()
            .type("chapter")
            .id(UUID.fromString("c0d54a6f-5340-43f2-bded-a9bea98eb9c3"))
            .parameters(List.of("Title:1", "Broken Links"))
            .build();

    static final Entry BROKEN_LINKS_ENTRY_2 = Entry.builder()
            .type("markdown")
            .id(UUID.fromString("b2863db7-39bd-4774-ba53-929fd0aad0d3"))
            .parameters(List.of("Test Driven Development"))
            .build();

    static final Chapter PROLOGUE = Chapter.builder()
            .chapterPath("00-prologue.json")
            .entry(PROLOGUE_ENTRY_1)
            .entry(PROLOGUE_ENTRY_2)
            .build();

    static final Chapter HELLO_WORLD = Chapter.builder()
            .chapterPath("01-hello-world.json")
            .entry(HELLO_WORLD_ENTRY_1)
            .entry(HELLO_WORLD_ENTRY_2)
            .build();

    static final Chapter BROKEN_LINKS = Chapter.builder()
            .chapterPath("02-broken-links.json")
            .entry(BROKEN_LINKS_ENTRY_1)
            .entry(BROKEN_LINKS_ENTRY_2)
            .build();

    static final Book BOOK = Book.builder()
            .title("Programming")
            .description("A book about programming")
            .bookPath(resolve("book.json"))
            .chapter(PROLOGUE)
            .chapter(HELLO_WORLD)
            .chapter(BROKEN_LINKS)
            .build();

    static Path resolve(final String name) {
        return BOOK_DIRECTORY.resolve(name);
    }
}
