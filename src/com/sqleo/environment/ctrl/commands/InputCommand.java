package com.sqleo.environment.ctrl.commands;

import java.util.List;
import java.util.regex.Pattern;

public class InputCommand extends AbstractCommand {

	public static final String NAME = "input";
	private static final String USAGE =
			"Usage: input <filename.sql>, Description: Run the commands provided in input file";

	public String filename;
	
	private void init(){
		filename = null;
	}

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
		return 2;
	}
	
	@Override
	protected Pattern getCommandRegex(){
		return Pattern.compile("(^input) (.*\\.sql$)");
	}

	@Override
	public CommandExecutionResult execute(String command) {
		init();
		final List<String> tokens = tokenizeCommand(command);
		if (tokens.isEmpty()) {
			return invalidCommandError(command);
		}
		final CommandExecutionResult result = new CommandExecutionResult();
		filename = tokens.get(1);
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

}
