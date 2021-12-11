package aa.sw.command;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            final ServerHttpRequest request,
            final WebSocketHandler wsHandler,
            final Map<String, Object> attributes) {
        return new StompPrincipal(UUID.randomUUID().toString());
    }
}
