package aa.sw.command.run;

import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/* TODO: This tests fails on CI (GitHub Actions).  More investigation is needed. */
@DisabledIfSystemProperty(named = "CI", matches = "true")
class ProcessRunnerTest {

    private BufferedOutput output;
    private CommandRunnerContext context;

    @BeforeEach
    void setUp() {
        output = new BufferedOutput();
        context = CommandRunnerContext.builder()
                .output(output)
                .build();
    }

    @Test
    void runSuccessfulProcess() {
        /* Given */
        final ScriptConfiguration configuration = ScriptConfiguration.builder().build();
        createScript(configuration);

        /* When */
        final ProcessResult result = ProcessRunner.builder()
                .command(List.of("./run"))
                .workspace(createWorkingDirectory())
                .context(context)
                .build()
                .run();

        /* Then */
        assertEquals(ProcessResult.finishedWithExitValue(0), result);

        final String expected = """
                Albert Attard
                """;
        assertEquals(expected, output.toString());
    }

    @Test
    void runTimeoutProcess() {
        /* Given */
        final ScriptConfiguration configuration = ScriptConfiguration.builder().delayInSeconds(2).build();
        createScript(configuration);

        /* When */
        final ProcessResult result = ProcessRunner.builder()
                .command(List.of("./run"))
                .workspace(createWorkingDirectory())
                .context(context)
                .commandTimeout(Duration.ofSeconds(1))
                .build()
                .run();

        /* Then */
        assertEquals(ProcessResult.timedOut(), result);

        final String expected = """
                Albert Attard
                                
                ================================================================================
                Process timed out and was killed
                ================================================================================
                """;
        assertEquals(expected, output.toString());
    }

    @Test
    void runProcessThatWritesToBothOutAndErrorStreams() {
        /* Given */
        final ScriptConfiguration configuration = ScriptConfiguration.builder()
                .output(ScriptConfiguration.OsOutput.STD_ERR).build();
        createScript(configuration);

        /* When */
        final ProcessResult result = ProcessRunner.builder()
                .command(List.of("./run"))
                .workspace(createWorkingDirectory())
                .context(context)
                .build()
                .run();

        /* Then */
        assertEquals(ProcessResult.finishedWithExitValue(0), result);

        final String expected = """
                Albert Attard
                """;
        assertEquals(expected, output.toString());
    }

    @Test
    void runProcessThatReturnSpecificExitValue() {
        /* Given */
        final ScriptConfiguration configuration = ScriptConfiguration.builder().exitValue(1).build();
        createScript(configuration);

        /* When */
        final ProcessResult result = ProcessRunner.builder()
                .command(List.of("./run"))
                .workspace(createWorkingDirectory())
                .context(context)
                .build()
                .run();

        /* Then */
        assertEquals(ProcessResult.finishedWithExitValue(1), result);

        final String expected = """
                Albert Attard
                """;
        assertEquals(expected, output.toString());
    }

    @Test
    void runProcessUseEnvironmentVariables() {
        /* Given */
        final ScriptConfiguration configuration = ScriptConfiguration.builder()
                .environmentVariable(Optional.of("NAME")).build();
        createScript(configuration);

        /* When */
        final ProcessResult result = ProcessRunner.builder()
                .command(List.of("./run"))
                .workspace(createWorkingDirectory())
                .context(context)
                .environmentVariables(Map.of("NAME", "Albert and James"))
                .build()
                .run();

        /* Then */
        assertEquals(ProcessResult.finishedWithExitValue(0), result);

        final String expected = """
                Albert and James
                """;
        assertEquals(expected, output.toString());
    }

    private static File createWorkingDirectory() {
        final File workingDirectory = new File("build/command-runner");
        assertTrue(workingDirectory.exists() || workingDirectory.mkdirs());

        return workingDirectory;
    }

    private void createScript(final ScriptConfiguration configuration) {
        final String bash = formatScript(configuration);

        final File bashFile = new File(createWorkingDirectory(), "run");
        writeToFile(bash, bashFile);
        assertTrue(bashFile.setExecutable(true));
    }

    private String formatScript(final ScriptConfiguration configuration) {
        return """
                #!/usr/bin/java --source 16
                                
                import java.util.Optional;
                import java.util.concurrent.TimeUnit;
                                
                public class HelloWorld {
                    public static void main(final String[] args) throws InterruptedException {
                        final String name = Optional
                                              .ofNullable(System.getenv("{ENVIRONMENT_VARIABLE}"))
                                              .orElse("Albert Attard");
                        System.{OS_OUTPUT}.println(name);
                        TimeUnit.SECONDS.sleep({DELAY_IN_SECONDS});
                        System.exit({EXIT_VALUE});
                    }
                }
                """
                .replaceAll("\\{ENVIRONMENT_VARIABLE}", configuration.getEnvironmentVariable().orElse(""))
                .replaceAll("\\{OS_OUTPUT}", configuration.getOutput().code())
                .replaceAll("\\{DELAY_IN_SECONDS}", String.valueOf(configuration.getDelayInSeconds()))
                .replaceAll("\\{EXIT_VALUE}", String.valueOf(configuration.getExitValue()));
    }

    private static void writeToFile(final String text, final File file) {
        try {
            Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write text to file " + file, e);
        }
    }

    public static class BufferedOutput implements Consumer<String> {
        final StringBuilder buffer = new StringBuilder();

        @Override
        public void accept(final String text) {
            buffer.append(text);
        }

        public String toString() {
            return buffer.toString();
        }
    }

    @Value
    @Builder
    private static class ScriptConfiguration {
        @Builder.Default
        int exitValue = 0;
        @Builder.Default
        int delayInSeconds = 0;
        @Builder.Default
        OsOutput output = OsOutput.STD_OUT;
        @Builder.Default
        Optional<String> environmentVariable = Optional.empty();

        enum OsOutput {
            STD_OUT("out"),
            STD_ERR("err");

            private final String code;

            OsOutput(String code) { this.code = code; }

            public String code() {
                return code;
            }
        }
    }
}
