package aa.sw.command.run;

import aa.sw.command.CommandResult;

@FunctionalInterface
public interface RunnableEntryExecutionStrategy {

    CommandResult execute(CommandRunnerContext context);
}
