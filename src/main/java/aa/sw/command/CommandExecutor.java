package aa.sw.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

    public CommandResult run(final Command entry, final Consumer<String> output) {
        LOGGER.debug("Running {}", entry);
        return CommandResult.failedToStart();
    }
}
