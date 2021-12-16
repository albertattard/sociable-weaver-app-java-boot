package aa.sw.config;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@AllArgsConstructor
public class CorsConfiguration implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorsConfiguration.class);

    private final AppProperties properties;

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        final String allowedOrigin = properties.getAllowedOrigin();
        LOGGER.debug("Registering allowed origin: '{}'", allowedOrigin);
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "PUT", "OPTIONS");
    }
}
