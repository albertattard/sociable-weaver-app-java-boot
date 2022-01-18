package aa.sw.book;

import aa.sw.common.Pair;
import aa.sw.common.Result;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
public class BookService {

    private final BookData data;

    public Result<Book> openBook(final BookPath bookPath) {
        requireNonNull(bookPath);

        return data.readBook(bookPath.getPath());
    }

    public Result<Chapter> readChapter(final ChapterPath chapterPath) {
        requireNonNull(chapterPath);

        return data.readChapter(chapterPath.getPath());
    }

    public Result<Entry> saveEntry(final ChapterPath chapterPath, final Entry entry) {
        requireNonNull(chapterPath);
        requireNonNull(entry);

        return readChapter(chapterPath)
                .then(chapter -> indexOfEntryInChapter(entry.getId(), chapter))
                .then(pair -> pair.left().swapEntryAt(pair.right(), entry))
                .then(updated -> writeChapter(Pair.of(chapterPath, updated)))
                .then(chapter -> chapter.findEntryWithId(entry.getId()))
                .then(entryIndex -> entryIndex.map(Chapter.EntryIndex::getEntry)
                        .orElseThrow(() -> new RuntimeException("The entry was not found in file after it was saved")));
    }

    public Result<Entry> createEntry(final ChapterPath chapterPath, final CreateEntry createEntry) {
        requireNonNull(chapterPath);
        requireNonNull(createEntry);

        /* TODO: We need to check if this exists */
        final UUID id = UUID.randomUUID();

        return readChapter(chapterPath)
                .then(chapter -> indexOfEntryInChapter(createEntry.getAfterEntryWithId(), chapter))
                .then(pair -> {
                    final Entry entry = Entry
                            .builder()
                            .id(id)
                            .type(createEntry.getType())
                            .build();
                    final Chapter chapter = pair.left().insertEntryAt(pair.right() + 1, entry);
                    return Pair.of(chapter, entry);
                })
                .then(pair -> writeChapter(Pair.of(chapterPath, pair.left())))
                .then(chapter -> chapter.findEntryWithId(id))
                .then(entryIndex -> entryIndex.map(Chapter.EntryIndex::getEntry)
                        .orElseThrow(() -> new RuntimeException("The entry was not found in file after it was created")));
    }

    public Result<Entry> deleteEntry(final ChapterPath chapterPath, final UUID entryId) {
        requireNonNull(chapterPath);
        requireNonNull(entryId);

        return readChapter(chapterPath)
                .then(chapter -> indexOfEntryInChapter(entryId, chapter))
                .then(pair -> pair.left().deleteEntryAt(pair.right()))
                .then(pair -> {
                    writeChapter(Pair.of(chapterPath, pair.left()));
                    return pair.right();
                });
    }

    private static Pair<Chapter, Integer> indexOfEntryInChapter(final UUID entryId, final Chapter chapter) {
        requireNonNull(entryId);
        requireNonNull(chapter);

        return chapter.findEntryWithId(entryId)
                .map(index -> Pair.of(chapter, index.getIndex()))
                .orElseThrow(EntryNotFoundException::new);

    }

    private Chapter writeChapter(final Pair<ChapterPath, Chapter> chapterAndPath) {
        requireNonNull(chapterAndPath);
        return chapterAndPath
                .with((path, chapter) -> data.writeChapter(path.getPath(), chapter))
                .right();
    }
}
