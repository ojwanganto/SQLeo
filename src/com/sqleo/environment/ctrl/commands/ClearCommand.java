package com.sqleo.environment.ctrl.commands;

import java.util.List;
import java.util.regex.Pattern;

public class ClearCommand extends AbstractCommand {

	private static final String USAGE = "Usage: clear, Description: Clears the command editor output text";
	public static String NAME = "clear";

	@Override
	public String getCommand() {
		return NAME;
	}

	@Override
	public String getCommandUsage() {
		return USAGE;
	}

	@Override
	public List<String> getCommandOptions() {
		// no options
		return null;
	}

	@Override
	public int getCommandTokensLength() {
		return 1;
	}
	
	@Override
	protected Pattern getCommandRegex(){
		return Pattern.compile("(^clear)");
	}

	@Override
	public CommandExecutionResult execute(String command) {
		final List<String> tokens = tokenizeCommand(command);
		if (tokens.isEmpty()) {
			return invalidCommandError(command);
		}
		final String givenCmd = tokens.get(0);
		final CommandExecutionResult result = new CommandExecutionResult();
		if (!givenCmd.equals(NAME)) {
			return invalidCommandError(result, "Given option: " + givenCmd + " is invalid, expected:" + NAME + " see \n" + USAGE);
		}
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

}
