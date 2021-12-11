package aa.sw.command.exec;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CommandFormatter {

    private final Optional<String> workingDirectory;
    private final char commandPromptSymbol;
    private final Optional<List<String>> environmentVariables;
    private final List<String> command;

    private CommandFormatter(final Builder builder) {
        requireNonNull(builder);

        this.workingDirectory = builder.workingDirectory;
        this.commandPromptSymbol = builder.commandPromptSymbol;
        this.environmentVariables = builder.environmentVariables;
        this.command = builder.command;
    }

    public static Builder builder(final String... command) {
        return builder(Arrays.asList(command));
    }

    public static Builder builder(final List<String> command) {
        return new Builder(command);
    }

    public static String format(final File workingDirectory, final String... command) {
        return format(Optional.of(workingDirectory.toString()), command);
    }

    public static String format(final File workingDirectory, final List<String> command) {
        return format(Optional.of(workingDirectory.toString()), command);
    }

    public static String format(final Optional<String> workingDirectory, final String... command) {
        return builder(command)
                .workingDirectory(workingDirectory)
                .format();
    }

    public static String format(final Optional<String> workingDirectory, final List<String> command) {
        return builder(command)
                .workingDirectory(workingDirectory)
                .format();
    }

    public static String format(final Optional<String> workingDirectory,
                                final Optional<List<String>> environmentVariables,
                                final String... command) {
        return builder(command)
                .workingDirectory(workingDirectory)
                .environmentVariables(environmentVariables)
                .format();
    }

    public String format() {
        return formattedWorkingDirectory()
                .concat(formatCommandPromptSymbol())
                .concat(formattedEnvironmentVariable())
                .concat(formattedCommand());
    }

    private String formattedWorkingDirectory() {
        return workingDirectory
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
        return command.stream()
                .map(CommandFormatter::wrapInQuotesIfRequired)
                .map(CommandFormatter::prefixWithWhiteSpace)
                .collect(Collectors.joining());
    }

    private static String wrapInQuotesIfRequired(final String commandOrArgument) {
        if (commandOrArgument.contains("!")) {
            return String.format("'%s'", commandOrArgument);
        }

        if (commandOrArgument.contains(" ") && !commandOrArgument.contains("\"")) {
            return String.format("\"%s\"", commandOrArgument);
        }

        return commandOrArgument;
    }

    private static String prefixWithWhiteSpace(final String text) {
        return " " + text;
    }

    public static class Builder {

        private Optional<String> workingDirectory = Optional.empty();
        private char commandPromptSymbol = '$';
        private Optional<List<String>> environmentVariables = Optional.empty();
        private List<String> command;

        private Builder(final List<String> command) {
            command(command);
        }

        public Builder workingDirectory(final Optional<String> workingDirectory) {
            this.workingDirectory = requireNonNull(workingDirectory);
            return this;
        }

        public Builder commandPromptSymbol(final char commandPromptSymbol) {
            this.commandPromptSymbol = commandPromptSymbol;
            return this;
        }

        public Builder environmentVariables(final Optional<List<String>> environmentVariables) {
            this.environmentVariables = requireNonNull(environmentVariables);
            return this;
        }

        public Builder command(final List<String> command) {
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
