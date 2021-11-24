package aa.sw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CorsHeadersTest {

    @Autowired
    private AppProperties properties;

    @Autowired
    private transient TestRestTemplate restTemplate;

    static {
        /* NOTE: RestTemplate makes use of the HttpUrlConnection class, which has the 'Origin' header as one of the
           restricted headers.  The following will make the HttpUrlConnection send the 'Origin' header. */
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    @Test
    void containsTheAllowedOriginInTheResponseHeaders() {
        /* Given */
        final HttpEntity<?> entity = createEntityWithOrigin(properties.getAllowedOrigin());

        /* When */
        final ResponseEntity<String> response = sendRequest(entity);

        /* Then */
        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getAccessControlAllowOrigin())
                .describedAs("The Access-Control-Allow-Origin header should be set")
                .isEqualTo(properties.getAllowedOrigin());
    }

    @Test
    void doesNotContainTheAllowedOriginInTheResponseHeadersAsTheOriginIsUnknown() {
        /* Given */
        final String origin = "https://somewhere.com";
        final HttpEntity<?> entity = createEntityWithOrigin(origin);

        /* When */
        final ResponseEntity<String> response = sendRequest(entity);

        /* Then */
        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getAccessControlAllowOrigin())
                .describedAs("The Access-Control-Allow-Origin header should not be set")
                .isNull();
    }

    @Test
    void doesNotContainTheAllowedOriginInTheResponseHeadersIfTheOriginIsMissingInRequest() {
        /* Given */
        final HttpEntity<?> entity = createEntityWithoutOrigin();

        /* When */
        final ResponseEntity<String> response = sendRequest(entity);

        /* Then */
        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getAccessControlAllowOrigin())
                .describedAs("The Access-Control-Allow-Origin header should not be set")
                .isNull();
    }

    private ResponseEntity<String> sendRequest(final HttpEntity<?> entity) {
        requireNonNull(entity);

        return restTemplate.exchange("/demo", HttpMethod.GET, entity, String.class);
    }

    private HttpEntity<String> createEntityWithOrigin(final String origin) {
        requireNonNull(origin);

        final HttpHeaders headers = new HttpHeaders();
        headers.setOrigin(origin);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<String> createEntityWithoutOrigin() {
        final HttpHeaders headers = new HttpHeaders();
        return new HttpEntity<>(headers);
    }
}
