package aa.sw.command;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class CommandOutput {
    @NonNull String content;
}
