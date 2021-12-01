package aa.sw.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class HelloController {

    @GetMapping({ "/api/hello" })
    public Map<String, String> demo() {
        return Collections.emptyMap();
    }
}
