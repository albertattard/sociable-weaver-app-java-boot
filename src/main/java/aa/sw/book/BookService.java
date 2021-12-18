package aa.sw.book;

import aa.sw.common.CustomPrettyPrinter;
import aa.sw.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

@Service
public class BookService {

    private final ObjectMapper reader;
    private final ObjectWriter writer;

    public BookService(final ObjectMapper mapper) {
        requireNonNull(mapper);

        this.reader = mapper;
        this.writer = CustomPrettyPrinter.of(mapper);
    }

    public Result<Book> openBook(final BookPath bookPath) {
        requireNonNull(bookPath);

        return Result.of(() -> reader.readValue(bookPath.getFile(), Book.class)
                .withBookPath(bookPath.getPath()));
    }

    public Result<Chapter> readChapter(final ChapterPath chapterPath) {
        requireNonNull(chapterPath);

        return Result.of(() -> reader.readValue(chapterPath.getFile(), Chapter.class));
    }

    public Result<Chapter.Entry> saveEntry(final ChapterPath chapterPath, final Chapter.Entry entry) {
        requireNonNull(chapterPath);
        requireNonNull(entry);

        return readChapter(chapterPath)
                .flatThen(chapter -> {
                    final int index = chapter.indexOf(entry);
                    if (index == -1) {
                        return Result.error(new EntryNotFoundException());
                    }

                    final Chapter updated = chapter
                            .swapEntryAt(index, entry);

                    try {
                        writer.writeValue(chapterPath.getFile(), updated);
                    } catch (final IOException e) {
                        return Result.error(e);
                    }

                    return readChapter(chapterPath)
                            .then(a -> a.findEntryWithId(entry.getId()))
                            .then(a -> a.map(Chapter.EntryIndex::getEntry)
                                    .orElseThrow(() -> new RuntimeException("")));
                });
    }
}
