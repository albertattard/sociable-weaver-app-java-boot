package aa.sw.book;

import aa.sw.common.CustomPrettyPrinter;
import aa.sw.common.Pair;
import aa.sw.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

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
                .then(chapter -> indexOfEntryInChapter(entry.getId(), chapter))
                .then(pair -> pair.left().swapEntryAt(pair.right(), entry))
                .then(updated -> writeChapter(Pair.of(chapterPath, updated)))
                .flatThen(pair -> readChapter(pair.left()))
                .then(chapter -> chapter.findEntryWithId(entry.getId()))
                .then(entryIndex -> entryIndex.map(Chapter.EntryIndex::getEntry)
                        .orElseThrow(() -> new RuntimeException("The entry was not found in file after it was saved")));
    }

    public Result<Chapter.Entry> createEntry(final ChapterPath chapterPath, final CreateEntry createEntry) {
        requireNonNull(chapterPath);
        requireNonNull(createEntry);

        final UUID id = UUID.randomUUID();

        return readChapter(chapterPath)
                .then(chapter -> indexOfEntryInChapter(createEntry.getAfterEntryWithId(), chapter))
                .then(pair -> {
                    final Chapter.Entry entry = Chapter.Entry
                            .builder()
                            .id(id)
                            .type(createEntry.getType())
                            .build();
                    final Chapter chapter = pair.left().insertEntryAt(pair.right() + 1, entry);
                    return Pair.of(chapter, entry);
                })
                .then(pair -> writeChapter(Pair.of(chapterPath, pair.left())))
                .flatThen(pair -> readChapter(pair.left()))
                .then(chapter -> chapter.findEntryWithId(id))
                .then(entryIndex -> entryIndex.map(Chapter.EntryIndex::getEntry)
                        .orElseThrow(() -> new RuntimeException("The entry was not found in file after it was created")));
    }

    public Result<Chapter.Entry> deleteEntry(final ChapterPath chapterPath, final UUID entryId) {
        return Result.error(new UnsupportedOperationException("Not yet implemented"));
    }

    private static Pair<Chapter, Integer> indexOfEntryInChapter(final UUID id, final Chapter chapter) {
        requireNonNull(id);
        requireNonNull(chapter);

        final int index = chapter.indexOf(id);
        if (index == -1) {
            throw new EntryNotFoundException();
        }

        return Pair.of(chapter, index);
    }

    private Pair<ChapterPath, Chapter> writeChapter(final Pair<ChapterPath, Chapter> chapterAndPath) throws IOException {
        requireNonNull(chapterAndPath);

        return chapterAndPath.with((path, chapter) -> writer.writeValue(path.getFile(), chapter));
    }
}
