package aa.sw.common;

import aa.sw.config.JacksonConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomPrettyPrinterTest {

    @Test
    void formatEmptyObject() {
        /* Given */
        final TestObject testObject = new TestObject();

        /* When */
        final String json = asString(testObject);

        /* Then */
        assertEquals(read("empty-object.json"), json);
    }

    @Test
    void formatSingleObject() {
        /* Given */
        final TestObject testObject = new TestObject();
        testObject.setName("Albert");

        /* When */
        final String json = asString(testObject);

        /* Then */
        assertEquals(read("single-object.json"), json);
    }

    @Test
    void formatSingleObjectWithEmptyArray() {
        /* Given */
        final TestObject testObject = new TestObject();
        testObject.setNames(new String[]{});

        /* When */
        final String json = asString(testObject);

        /* Then */
        assertEquals(read("single-object-with-empty-array.json"), json);
    }

    @Test
    void formatSingleObjectWithArray() {
        /* Given */
        final TestObject testObject = new TestObject();
        testObject.setNames(new String[]{"Albert", "Attard"});

        /* When */
        final String json = asString(testObject);

        /* Then */
        assertEquals(read("single-object-with-array.json"), json);
    }

    private static String asString(final TestObject testObject) {
        final ObjectWriter writer = CustomPrettyPrinter.of(new JacksonConfiguration().createObjectMapper());

        try {
            return writer.writeValueAsString(testObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write value as JSON string", e);
        }
    }

    @Data
    private static class TestObject {
        private String name;
        private String[] names;
    }

    private String read(final String name) {
        try {
            return Files
                    .readString(Path.of("src/test/resources/fixtures/formatting", name))
                    .stripTrailing() /* Trailing new line r*/;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read file", e);
        }
    }
}
