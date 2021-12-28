package aa.sw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RestTemplateWrapper {

    @Autowired
    private TestRestTemplate restTemplate;

    static {
        /* NOTE: RestTemplate and TestRestTemplate makes use of the HttpUrlConnection class, which has the 'Origin'
            header as one of the restricted headers.  The following will make the HttpUrlConnection send the 'Origin'
            header. Do not use the RestTemplate or TestRestTemplate directly, but go through this class as otherwise the
            following property may not be properly set. */
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    public <T> ResponseEntity<T> exchange(final String url, final HttpMethod method, final HttpEntity<?> requestEntity, final Class<T> responseType, final Object... urlVariables) {return restTemplate.exchange(url, method, requestEntity, responseType, urlVariables);}
}
