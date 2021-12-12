package aa.sw.command.run;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CommandFormatter {

    private final char commandPromptSymbol;
    private final Optional<List<String>> environmentVariables;
    private final Command command;

    private CommandFormatter(final Builder builder) {
        requireNonNull(builder);

        this.commandPromptSymbol = builder.commandPromptSymbol;
        this.environmentVariables = builder.environmentVariables;
        this.command = builder.command;
    }

    public static Builder builder(final String... parameters) {
        return builder(Arrays.asList(parameters));
    }

    public static Builder builder(final List<String> parameters) {
        return builder(Command.parse(parameters));
    }

    public static Builder builder(final Command command) {
        return new Builder(command);
    }

    public static String format(final Command command) {
        return builder(command)
                .format();
    }

    public String format() {
        return formattedWorkingDirectory()
                .concat(formatCommandPromptSymbol())
                .concat(formattedEnvironmentVariable())
                .concat(formattedCommand());
    }

    private String formattedWorkingDirectory() {
        return command.getWorkingDirectory()
                .map(CommandFormatter::formatWorkingDirectory)
                .orElse("");
    }

    private static String formatWorkingDirectory(final String workingDirectory) {
        return workingDirectory + " ";
    }

    private String formatCommandPromptSymbol() {
        return String.format("%s", commandPromptSymbol);
    }

    private String formattedEnvironmentVariable() {
        return environmentVariables
                .map(CommandFormatter::formatEnvironmentVariables)
                .orElse("");
    }

    private static String formatEnvironmentVariables(final List<String> list) {
        return list.stream()
                .map(CommandFormatter::formatEnvironmentVariable)
                .collect(Collectors.joining());
    }

    private static String formatEnvironmentVariable(final String environmentVariable) {
        return String.format(" %s=<%s>;", environmentVariable, environmentVariable.replaceAll("_+", "-"));
    }

    private String formattedCommand() {
        return command.toString();
    }

    public static class Builder {

        private char commandPromptSymbol = '$';
        private Optional<List<String>> environmentVariables = Optional.empty();
        private Command command;

        private Builder(final Command command) {
            command(command);
        }

        public Builder commandPromptSymbol(final char commandPromptSymbol) {
            this.commandPromptSymbol = commandPromptSymbol;
            return this;
        }

        public Builder environmentVariables(final Optional<List<String>> environmentVariables) {
            this.environmentVariables = requireNonNull(environmentVariables);
            return this;
        }

        public Builder command(final Command command) {
            this.command = requireNonNull(command);
            return this;
        }

        public CommandFormatter build() {
            return new CommandFormatter(this);
        }

        public String format() {
            return build().format();
        }
    }
}
