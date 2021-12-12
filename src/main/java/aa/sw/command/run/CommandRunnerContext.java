package aa.sw.command.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class CommandRunnerContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandRunnerContext.class);

    private static final String BLOCK_LINE =
            "================================================================================";

    private static final String THIN_LINE =
            "--------------------------------------------------------------------------------";

    private final Consumer<String> outputConsumer;

    private CommandRunnerContext(final Builder builder) {
        this.outputConsumer = builder.outputConsumer;
    }

    public void appendError(final String message, final Throwable error) {
        wrapInBlock(() -> {
            outputConsumer.accept(String.format("%s - %s%n", message, error));
            outputConsumer.accept(suffixNewLine(THIN_LINE));
            outputConsumer.accept(formatStackTrace(error));
        });
    }

    public void appendError(final String message) {
        wrapInBlock(() -> outputConsumer.accept(suffixNewLine(message)));
    }

    public void appendErrorF(final String format, Object... params) {
        if (params.length > 0 && params[params.length - 1] instanceof Throwable e) {
            appendError(String.format(format, params), e);
        } else {
            appendLineF(format, params);
        }
    }

    private String formatStackTrace(final Throwable error) {
        final StringWriter buffer = new StringWriter();
        try (PrintWriter out = new PrintWriter(buffer)) {
            error.printStackTrace(out);
        }
        return buffer.toString();
    }

    private void wrapInBlock(final Runnable runnable) {
        outputConsumer.accept(suffixNewLine(""));
        outputConsumer.accept(suffixNewLine(BLOCK_LINE));
        runnable.run();
        outputConsumer.accept(suffixNewLine(BLOCK_LINE));
    }

    public void appendLine(final String line) {
        requireNonNull(line);

        outputConsumer.accept(suffixNewLine(line));
    }

    public void appendLineF(final String format, final Object... params) {
        requireNonNull(format);

        appendLine(String.format(format, params));
    }

    private String suffixNewLine(final Object object) {
        return String.format("%s%n", object);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Consumer<String> outputConsumer = line -> { LOGGER.debug("{}", line); };

        private Builder() { }

        public Builder output(final Consumer<String> outputConsumer) {
            requireNonNull(outputConsumer);

            this.outputConsumer = outputConsumer;
            return this;
        }

        public CommandRunnerContext build() {
            return new CommandRunnerContext(this);
        }
    }
}
