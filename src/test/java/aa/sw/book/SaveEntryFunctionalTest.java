package aa.sw.book;

import aa.sw.RestTemplateWrapper;
import aa.sw.common.Result;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SaveEntryFunctionalTest {

    @Autowired
    private RestTemplateWrapper restTemplate;

    @MockBean
    private BookService service;

    @Test
    void saveEntryWithCommandTimeout() {
        final Path bookPath = Path.of("path-to-book");
        final Path chapterPath = Path.of("path-to-chapter");
        final String url = String.format("/api/entry?bookPath=%s&chapterPath=%s",
                URLEncoder.encode(bookPath.toString(), StandardCharsets.UTF_8),
                URLEncoder.encode(chapterPath.toString(), StandardCharsets.UTF_8));

        final Chapter.Entry entry = Chapter.Entry.builder()
                .id(UUID.fromString("8c579296-5e56-4d41-ac74-8270432f5021"))
                .type("command")
                .commandTimeout(Duration.ofSeconds(300))
                .build();

        when(service.saveEntry(ChapterPath.of(bookPath, chapterPath), entry))
                .thenReturn(Result.value(entry));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final Map<String, Object> body = new HashMap<>();
        body.put("id", "8c579296-5e56-4d41-ac74-8270432f5021");
        body.put("type", "command");
        body.put("commandTimeout", 300);
        final HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        final ResponseEntity<EntryResponseBody> exchange = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, EntryResponseBody.class);

        assertThat(exchange.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody())
                .isEqualTo(new EntryResponseBody("command", UUID.fromString("8c579296-5e56-4d41-ac74-8270432f5021"), new BigDecimal("300.000000000")));
    }

    private record EntryResponseBody(String type, UUID id, BigDecimal commandTimeout) {}
}
