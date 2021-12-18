package aa.sw.book;

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
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

@Value
@Builder
@JsonDeserialize(builder = Chapter.ChapterBuilder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Chapter {

    List<Entry> entries;

    public int indexOf(final Entry entry) {
        requireNonNull(entry);

        return findEntryWithId(entry.getId())
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
        requireInRange(index);
        requireNonNull(entry);

        final Entry existing = entries.get(index);
        if (existing.equals(entry)) {
            return this;
        }

        final List<Entry> updated = new ArrayList<>(entries);
        updated.set(index, entry);
        return Chapter.ChapterBuilder.build(updated);
    }

    private int requireInRange(final int index) {
        if (index < 0 || index >= entries.size()) {
            throw new IllegalArgumentException(String.format("The index %d is out of range ([%d,%d))", index, 0, entries.size()));
        }

        return index;
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

        @JsonPOJOBuilder(withPrefix = "")
        public static class EntryBuilder { }
    }

    @Value
    public static class EntryIndex {
        int index;
        Entry entry;
    }
}
