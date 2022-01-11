package aa.sw.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@AllArgsConstructor
public class CorsConfiguration implements WebMvcConfigurer {

    private final AppProperties properties;

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        final String allowedOrigin = properties.getAllowedOrigin();
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("DELETE", "GET", "POST", "PUT", "OPTIONS");
    }
}
