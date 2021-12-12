package aa.sw.command.run;

import aa.sw.command.CommandResult;

@FunctionalInterface
public interface RunnableEntryExecutorStrategy {

    CommandResult execute(CommandRunnerContext context);
}
