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
                .then(a -> {
                    final Book.BookBuilder builder = Book.builder()
                            .title(a.getTitle())
                            .description(a.getDescription())
                            .bookPath(path);

                    for (final String b : a.getChapters()) {
                        final Result<Chapter> c = readChapter(path.resolve(b));
                        builder.chapter(c.value());
                    }

                    return builder.build();
                });
    }

    public Result<Chapter> readChapter(final Path path) {
        requireNonNull(path);

        return read(path, Chapter.class);
    }

    public Result<Chapter> writeChapter(final Path path, final Chapter chapter) {
        requireNonNull(path);
        requireNonNull(chapter);

        return write(path, Chapter.class, chapter);
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
    @Builder(toBuilder = true)
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

}
