package aa.sw.command.exec;

import aa.sw.command.CommandResult;

@FunctionalInterface
public interface RunnableEntryExecutorStrategy {

    CommandResult execute(CommandRunnerContext context);
}
