package aa.sw.config;

import lombok.NonNull;
import lombok.Value;

import java.security.Principal;

@Value
public class StompPrincipal implements Principal {

    @NonNull String name;

}
