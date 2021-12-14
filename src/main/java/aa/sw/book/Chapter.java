package aa.sw.book;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Value
@Builder
@JsonDeserialize(builder = Chapter.ChapterBuilder.class)
public class Chapter {

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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Entry {

        private static final Set<String> RUNNABLE_TYPES = createRunnableTypesSet();

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
        Boolean visible;
        Boolean sensitive;
        Integer expectedExitValue;
        Duration commandTimeout;

        public boolean isRunnable() {
            return RUNNABLE_TYPES.contains(type);
        }

        private static Set<String> createRunnableTypesSet() {
            return Set.of("command", "create", "docker-tag-and-push", "download", "git-apply-patch",
                    "git-commit-changes", "git-tag-current-commit", "replace");
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class EntryBuilder { }
    }
}
