package aa.sw.command.exec;

import aa.sw.command.CommandResult;

@FunctionalInterface
public interface CodeExecutorStrategy {

    CommandResult execute(CommandRunnerContext context);
}
