package aa.sw.book;

import aa.sw.common.Pair;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Chapter.ChapterBuilder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Chapter {

    String chapterPath;
    List<Entry> entries;

    public static Chapter of(final String chapterPath, final String title, final String description) {
        requireNonNull(chapterPath);
        requireNonNull(title);
        requireNonNull(description);

        return builder()
                .chapterPath(chapterPath)
                .entry(Entry.chapter(title, description))
                .build();
    }

    public String getTitle() {
        return readMultiPartProperty("chapter", "Title");
    }

    public String getDescription() {
        return readMultiPartProperty("chapter", "Description");
    }

    private String readMultiPartProperty(final String type, final String part) {
        return readMultiPartProperty(type, part, () -> "");
    }

    private String readMultiPartProperty(final String type, final String part, final Supplier<String> defaultValue) {
        return entries.stream()
                .filter(entry -> type.equals(entry.getType()))
                .map(Entry::getMultipartParameters)
                .map(parameters -> parameters.getPart(part))
                .map(parameters -> String.join("\n", parameters))
                .findFirst()
                .orElseGet(defaultValue);
    }

    public Optional<EntryIndex> findEntryWithId(final UUID id) {
        return findEntry(entry -> Objects.equals(id, entry.getId()));
    }

    public Optional<EntryIndex> findEntry(final Predicate<Entry> predicate) {
        requireNonNull(predicate);

        for (int i = 0; i < entries.size(); i++) {
            if (predicate.test(entries.get(i))) {
                return Optional.of(new EntryIndex(i, entries.get(i)));
            }
        }

        return Optional.empty();
    }

    public Chapter swapEntryAt(final int index, final Entry entry) {
        requireInIndexRange(index);
        requireNonNull(entry);

        final Entry existing = entries.get(index);
        if (existing.equals(entry)) {
            return this;
        }

        final List<Entry> updated = new ArrayList<>(entries);
        updated.set(index, entry);
        return withEntries(updated);
    }

    public Chapter insertEntryAt(final int index, final Entry entry) {
        requireInInsertRange(index);
        requireNonNull(entry);
        requireNonNull(entry.getId());

        return findEntryWithId(entry.getId())
                .map(this::duplicateEntryIdException)
                .orElseGet(() -> _insertEntryAt(index, entry));
    }

    public Pair<Chapter, Entry> deleteEntryAt(final int index) {
        requireInIndexRange(index);

        final List<Entry> updated = new ArrayList<>(entries.size() - 1);
        if (isNotTheFirstEntry(index)) {updated.addAll(entries.subList(0, index));}
        if (isNotTheLastEntry(index)) {updated.addAll(entries.subList(index + 1, entries.size()));}

        return Pair.of(withEntries(updated), entries.get(index));
    }

    private boolean isNotTheFirstEntry(final int index) {
        return index > 0;
    }

    private boolean isNotTheLastEntry(final int index) {
        return index < entries.size() - 1;
    }

    private Chapter _insertEntryAt(final int index, final Entry entry) {
        final List<Entry> updated = new ArrayList<>(entries.size() + 1);
        updated.addAll(entries);
        updated.add(index, entry);
        return withEntries(updated);
    }

    public <T> T map(final Function<Chapter, T> mapper) {
        requireNonNull(mapper);

        return mapper.apply(this);
    }

    private Chapter duplicateEntryIdException(final EntryIndex duplicate) {
        throw new DuplicateEntryIdException(duplicate.getEntry().getId());
    }

    private void requireInIndexRange(final int index) {
        if (index < 0 || index >= entries.size()) {
            throw new IllegalArgumentException(String.format("The index %d is out of range ([%d,%d))", index, 0, entries.size()));
        }
    }

    private void requireInInsertRange(final int index) {
        if (index < 0 || index > entries.size()) {
            throw new IllegalArgumentException(String.format("The index %d is out of range ([%d,%d])", index, 0, entries.size()));
        }
    }

    private Chapter withEntries(final List<Entry> entries) {
        requireNonNull(entries);

        return toBuilder().entries(entries).build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ChapterBuilder {

        private final List<Entry> entries = new ArrayList<>();

        public ChapterBuilder entry(final Entry entry) {
            requireNonNull(entry);

            entries.add(entry);
            return this;
        }

        public ChapterBuilder entries(final List<Entry> entries) {
            requireNonNull(entries);

            this.entries.clear();
            this.entries.addAll(entries);
            return this;
        }

        public Chapter build() {
            return build(chapterPath, entries);
        }

        public static Chapter build(final String chapterPath, final List<Entry> entries) {
            requireNonNull(chapterPath);
            requireNonNull(entries);

            return new Chapter(chapterPath, List.copyOf(entries));
        }
    }

    @Value
    public static class EntryIndex {
        int index;
        Entry entry;
    }
}
