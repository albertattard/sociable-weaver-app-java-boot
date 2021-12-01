package aa.sw.open;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class OpenController {

    private final OpenService service;

    @GetMapping("/open-local")
    public ResponseEntity<?> openLocal(@RequestParam("path") final Path path) {
        return service.openLocal(path)
                .map(ResponseEntity::ok, this::toErrorResponse);
    }

    private ResponseEntity<?> toErrorResponse(final Throwable e) {
        final String message = e instanceof FileNotFoundException
                ? "Folder not found"
                : "Encountered an unexpected error";

        return ResponseEntity.unprocessableEntity()
                .body(Map.of("message", message));
    }
}
