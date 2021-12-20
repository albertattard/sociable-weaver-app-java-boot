package aa.sw.book;

import aa.sw.common.Pair;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

@Value
@Builder
@JsonDeserialize(builder = Chapter.ChapterBuilder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Chapter {

    List<Entry> entries;

    public int indexOf(final UUID id) {
        return findEntryWithId(id)
                .map(EntryIndex::getIndex)
                .orElse(-1);
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
        return Chapter.ChapterBuilder.build(updated);
    }

    public Pair<Chapter, Entry> createEntryAt(final int index, final String type) {
        requireInInsertRange(index);
        requireNonNull(type);

        return randomUnusedEntryId()
                .map(id -> Entry.builder().id(id).type(type).build())
                .map(entry -> Pair.of(insertEntryAt(index, entry), entry))
                .orElseThrow(() -> new RuntimeException("Failed to find an unused entry id"));
    }

    public Chapter insertEntryAt(final int index, final Entry entry) {
        requireInInsertRange(index);
        requireNonNull(entry);
        requireNonNull(entry.getId());

        return findEntryWithId(entry.getId())
                .map(this::duplicateEntryIdException)
                .orElseGet(() -> _insertEntryAt(index, entry));
    }

    private Chapter _insertEntryAt(final int index, final Entry entry) {
        final List<Entry> updated = new ArrayList<>(entries.size() + 1);
        updated.addAll(entries);
        updated.add(index, entry);
        return ChapterBuilder.build(updated);
    }

    public <T> T map(final Function<Chapter, T> mapper){
        requireNonNull(mapper);

        return mapper.apply(this);
    }

    private Optional<UUID> randomUnusedEntryId() {
        for (int attempts = 0; attempts < 100; attempts++) {
            final UUID uuid = UUID.randomUUID();
            if (indexOf(uuid) == -1) {
                return Optional.of(uuid);
            }
        }

        return Optional.empty();
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
            return build(entries);
        }

        public static Chapter build(final List<Entry> entries) {
            requireNonNull(entries);

            return new Chapter(List.copyOf(entries));
        }
    }

    @Value
    @Builder(toBuilder = true)
    @JsonDeserialize(builder = Entry.EntryBuilder.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Entry {

        String type;
        UUID id;
        String name;
        String workingDirectory;
        List<String> parameters;
        List<String> variables;
        List<String> environmentVariables;
        Map<String, String> values;
        Boolean ignoreErrors;
        Boolean pushChanges;
        Boolean dryRun;
        Boolean sensitive;
        Integer expectedExitValue;
        Duration commandTimeout;

        public <T> T map(final Function<Entry, T> mapper) {
            requireNonNull(mapper);

            return mapper.apply(this);
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class EntryBuilder {}
    }

    @Value
    public static class EntryIndex {
        int index;
        Entry entry;
    }
}
