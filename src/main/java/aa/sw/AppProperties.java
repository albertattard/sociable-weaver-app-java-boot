package aa.sw;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String allowedOrigin;
}
