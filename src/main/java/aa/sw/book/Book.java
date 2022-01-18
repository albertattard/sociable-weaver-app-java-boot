package aa.sw.book;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Book {

    String title;
    String description;
    String bookPath;
    List<Chapter> chapters;

    public Book withBookPath(final Path bookPath) {
        requireNonNull(bookPath);

        return withBookPath(bookPath.toString());
    }

    public Book withBookPath(final String bookPath) {
        requireNonNull(bookPath);

        if (bookPath.equals(this.bookPath)) {
            return this;
        }

        return toBuilder().bookPath(bookPath).build();
    }

    public static class BookBuilder {
        private final List<Chapter> chapters = new ArrayList<>();

        public BookBuilder chapter(final Chapter chapter) {
            requireNonNull(chapter);

            chapters.add(chapter);
            return this;
        }

        public BookBuilder chapters(final List<Chapter> chapters) {
            requireNonNull(chapters);

            this.chapters.clear();
            this.chapters.addAll(chapters);
            return this;
        }

        public BookBuilder bookPath(final Path bookPath) {
            requireNonNull(bookPath);

            return bookPath(bookPath.toString());
        }

        public BookBuilder bookPath(final String bookPath) {
            this.bookPath = bookPath;
            return this;
        }

        public Book build() {
            return new Book(
                    requireNonNull(title, "The book title cannot be null"),
                    requireNonNull(description, "The book description cannot be null"),
                    bookPath,
                    List.copyOf(chapters)
            );
        }
    }
}
