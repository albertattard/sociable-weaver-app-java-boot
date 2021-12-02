package aa.sw.book;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Value
@Builder
@JsonDeserialize(builder = Chapter.ChapterBuilder.class)
public class Chapter {

    /* TODO: Should we have the title and description here too? */
    List<Entry> entries;

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
            return new Chapter(List.copyOf(entries));
        }
    }

    @Value
    @Builder
    @JsonDeserialize(builder = Entry.EntryBuilder.class)
    public static class Entry {

        String type;
        UUID id;
        Optional<String> name;
        Optional<String> workingDirectory;
        Optional<List<String>> parameters;
        Optional<List<String>> variables;
        Optional<Map<String, String>> values;
        Optional<Boolean> ignoreErrors;
        Optional<Boolean> pushChanges;
        Optional<Boolean> dryRun;
        Optional<Boolean> visible;
        Optional<Boolean> sensitive;
        OptionalInt expectedExitValue;
        Optional<Duration> commandTimeout;

        @JsonPOJOBuilder(withPrefix = "")
        public static class EntryBuilder {

            private Optional<String> name = Optional.empty();
            private Optional<String> workingDirectory = Optional.empty();
            private Optional<List<String>> parameters = Optional.empty();
            private Optional<List<String>> variables = Optional.empty();
            private Optional<Map<String, String>> values = Optional.empty();
            private Optional<Boolean> ignoreErrors = Optional.empty();
            private Optional<Boolean> pushChanges = Optional.empty();
            private Optional<Boolean> dryRun = Optional.empty();
            private Optional<Boolean> visible = Optional.empty();
            private Optional<Boolean> sensitive = Optional.empty();
            private OptionalInt expectedExitValue = OptionalInt.empty();
            private Optional<Duration> commandTimeout = Optional.empty();

            public EntryBuilder entry(final Entry entry) {
                requireNonNull(entry);

                type = entry.type;
                id = entry.id;
                name = entry.name;
                workingDirectory = entry.workingDirectory;
                parameters = entry.parameters;
                variables = entry.variables;
                values = entry.values;
                ignoreErrors = entry.ignoreErrors;
                pushChanges = entry.pushChanges;
                dryRun = entry.dryRun;
                visible = entry.visible;
                sensitive = entry.sensitive;
                expectedExitValue = entry.expectedExitValue;
                commandTimeout = entry.commandTimeout;

                return this;
            }

            public EntryBuilder id(final UUID id) {
                requireNonNull(id);

                this.id = id;
                return this;
            }

            public EntryBuilder id(final String id) {
                requireNonNull(id);

                return id(UUID.fromString(id));
            }

            public EntryBuilder name(final String name) {
                this.name = Optional.ofNullable(name);
                return this;
            }

            public EntryBuilder workingDirectory(final String workingDirectory) {
                this.workingDirectory = Optional.ofNullable(workingDirectory);
                return this;
            }

            public EntryBuilder parameters(final List<String> parameters) {
                this.parameters = toImmutable(parameters);
                return this;
            }

            public EntryBuilder variables(final List<String> variables) {
                this.variables = toImmutable(variables);
                return this;
            }

            public EntryBuilder values(final Map<String, String> values) {
                this.values = toImmutable(values);
                return this;
            }

            public EntryBuilder ignoreErrors(final Boolean ignoreErrors) {
                this.ignoreErrors = Optional.ofNullable(ignoreErrors);
                return this;
            }

            public EntryBuilder pushChanges(final Boolean pushChanges) {
                this.pushChanges = Optional.ofNullable(pushChanges);
                return this;
            }

            public EntryBuilder dryRun(final Boolean dryRun) {
                this.dryRun = Optional.ofNullable(dryRun);
                return this;
            }

            public EntryBuilder visible(final Boolean visible) {
                this.visible = Optional.ofNullable(visible);
                return this;
            }

            public EntryBuilder sensitive(final Boolean sensitive) {
                this.sensitive = Optional.ofNullable(sensitive);
                return this;
            }

            public EntryBuilder expectedExitValue(final Integer expectedExitValue) {
                this.expectedExitValue = Optional.ofNullable(expectedExitValue)
                        .map(OptionalInt::of)
                        .orElse(OptionalInt.empty());
                return this;
            }

            public EntryBuilder commandTimeout(final Duration commandTimeout) {
                this.commandTimeout = Optional.ofNullable(commandTimeout);
                return this;
            }

            private static Optional<List<String>> toImmutable(final List<String> list) {
                return Optional.ofNullable(list).map(List::copyOf);
            }

            private static Optional<Map<String, String>> toImmutable(final Map<String, String> map) {
                return Optional.ofNullable(map).map(Map::copyOf);
            }
        }
    }
}
