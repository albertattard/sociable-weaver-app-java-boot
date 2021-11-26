package aa.sw.open;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class OpenController {

    private final OpenService service;

    @PostMapping("/open-local")
    public ResponseEntity<?> openLocal(@RequestBody final OpenLocalRequest request) {
        final Path path = Path.of(request.getOpenFromFolder());
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
