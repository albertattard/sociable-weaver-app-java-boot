package aa.sw.command;

import aa.sw.command.exec.CommandExecutor;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Controller
@AllArgsConstructor
public class CommandController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandController.class);

    /* TODO: Should we have a gateway instead? */
    private final SimpMessagingTemplate simpMessagingTemplate;

    /* TODO: This will not work well with multi tenants */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final CommandExecutor code;

    @MessageMapping("/run")
    public void run(final Principal principal, final RunnableEntry entry) {
        executor.submit(() -> {
            final Consumer<String> output = createOutput(principal);
            final CommandResult commandResult = runCommand(entry, output);
            sentOutput(principal, commandResult.asString(), Topic.OUTCOME);
        });
    }

    private CommandResult runCommand(final RunnableEntry entry, final Consumer<String> output) {
        try {
            return code.run(entry, output);
        } catch (final RuntimeException e) {
            LOGGER.error("Failed run command {}", entry, e);
            output.accept("Failed to run command");
            return CommandResult.failedToStart();
        }
    }

    private Consumer<String> createOutput(final Principal principal) {
        return line -> sentOutput(principal, line, Topic.OUTPUT);
    }

    private void sentOutput(final Principal principal, final String line, final Topic topic) {
        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                topic.getDestination(),
                CommandOutput.of(line));
    }

    private enum Topic {
        OUTPUT("output"),
        OUTCOME("outcome");

        private final String destination;

        Topic(final String destination) { this.destination = "/topic/run/" + destination; }

        public String getDestination() {
            return destination;
        }
    }
}
