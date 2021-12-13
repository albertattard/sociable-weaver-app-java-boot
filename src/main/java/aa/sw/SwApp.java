package aa.sw;

import aa.sw.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SwApp {

    public static void main(final String[] args) {
        SpringApplication.run(SwApp.class, args);
    }

}
