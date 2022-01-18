package aa.sw.book;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartParametersTest {

    @Test
    void returnsEmptyArrayIfNameIsNotFound() {
        /* Given */
        final String name = "SOMETHING";
        final List<String> parameters = Collections.emptyList();
        final MultipartParameters multipart = MultipartParameters.of(parameters);

        /* When */
        final List<String> result = multipart.getPart(name);

        /* Then */
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void returnsThePartWithTheGivenName() {
        /* Given */
        final String name = "SOMETHING";
        final List<String> parameters = List.of("OTHER-1:1", "x", "SOMETHING:2", "A", "B", "OTHER-2:1", "y");
        final MultipartParameters multipart = MultipartParameters.of(parameters);

        /* When */
        final List<String> result = multipart.getPart(name);

        /* Then */
        assertThat(result).isEqualTo(List.of("A", "B"));
    }
}
