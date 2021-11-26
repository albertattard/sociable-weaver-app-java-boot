package aa.sw.open;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Value
@Builder
public class OpenValue {

    String title;
    String description;
    String path;
    List<Chapter> chapters;

    public static class OpenValueBuilder {
        private final List<Chapter> chapters = new ArrayList<>();

        public OpenValueBuilder chapter(final String title, final String description, final String path) {
            requireNonNull(title);
            requireNonNull(description);
            requireNonNull(path);

            return chapter(Chapter.builder().title(title).description(description).path(path).build());
        }

        public OpenValueBuilder chapter(final Chapter chapter) {
            requireNonNull(chapter);

            chapters.add(chapter);
            return this;
        }

        public OpenValueBuilder chapters(final List<Chapter> chapters) {
            requireNonNull(chapters);

            this.chapters.clear();
            this.chapters.addAll(chapters);
            return this;
        }

        public OpenValue build() {
            /* TODO: Add null checks for the rest */
            return new OpenValue(title, description, path, List.copyOf(chapters));
        }
    }

    @Value
    @Builder
    public static class Chapter {
        String title;
        String description;
        String path;
    }
}
