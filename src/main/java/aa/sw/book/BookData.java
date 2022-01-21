package aa.sw.book;

import aa.sw.common.CustomPrettyPrinter;
import aa.sw.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        requireNonNull(path);

        return read(path, BookFile.class)
                .flatThen(bookFile -> {
                    final Book.BookBuilder builder = Book.builder()
                            .title(bookFile.getTitle())
                            .description(bookFile.getDescription())
                            .bookPath(path);

                    for (final String chapterPath : bookFile.getChapters()) {
                        final Result<ChapterFile> result = read(path.resolveSibling(chapterPath), ChapterFile.class);
                        if (!result.isValuePresent()) {
                            return Result.error(result.error());
                        }

                        final Chapter chapter = Chapter.builder()
                                .chapterPath(chapterPath)
                                .entries(result.value().getEntries())
                                .build();
                        builder.chapter(chapter);
                    }

                    return Result.value(builder.build());
                });
    }

    public Result<Chapter> readChapter(final Path path) {
        requireNonNull(path);

        return read(path, ChapterFile.class)
                .then(f -> Chapter.builder()
                        /* TODO: This is wrong */
                        .chapterPath(path.getFileName().toString())
                        .entries(f.entries)
                        .build());
    }

    public Result<Chapter> writeChapter(final Path path, final Chapter chapter) {
        requireNonNull(path);
        requireNonNull(chapter);

        return write(path, ChapterFile.class, ChapterFile.of(chapter))
                .then(f -> Chapter.builder()
                        .chapterPath(chapter.getChapterPath())
                        .entries(f.entries)
                        .build());
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

    @Value
    @Builder
    @JsonDeserialize(builder = BookFile.BookFileBuilder.class)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class BookFile {

        String title;
        String description;
        List<String> chapters;

        @JsonPOJOBuilder(withPrefix = "")
        public static class BookFileBuilder {
            private final List<String> chapters = new ArrayList<>();

            public BookFileBuilder chapter(final String path) {
                requireNonNull(path);

                chapters.add(path);
                return this;
            }

            public BookFileBuilder chapters(final List<String> chapters) {
                requireNonNull(chapters);

                this.chapters.clear();
                this.chapters.addAll(chapters);
                return this;
            }

            public BookFile build() {
                return new BookFile(
                        requireNonNull(title, "The book title cannot be null"),
                        requireNonNull(description, "The book description cannot be null"),
                        List.copyOf(chapters)
                );
            }
        }
    }

    @Value
    @Builder
    @JsonDeserialize(builder = ChapterFile.ChapterFileBuilder.class)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ChapterFile {
        List<Entry> entries;

        public static ChapterFile of(final Chapter chapter) {
            requireNonNull(chapter);

            return builder().entries(chapter.getEntries()).build();
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class ChapterFileBuilder {

            private final List<Entry> entries = new ArrayList<>();

            public ChapterFileBuilder entry(final Entry entry) {
                requireNonNull(entry);

                entries.add(entry);
                return this;
            }

            public ChapterFileBuilder entries(final List<Entry> entries) {
                requireNonNull(entries);

                this.entries.clear();
                this.entries.addAll(entries);
                return this;
            }

            public ChapterFile build() {
                return new ChapterFile(List.copyOf(entries));
            }
        }
    }
}
