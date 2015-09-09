package com.sqleo.environment.ctrl.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class InputCommand extends AbstractCommand {

	public static final String NAME = "input";
	private static final String USAGE =
			"Usage: input <filename.sql> echo|noecho, Description: Run the commands provided in input file, echo will display the commands by default";
	private static final List<String> OPTIONS = Arrays.asList("echo", "noecho");

	public String filename;
	public boolean echo;
	
	private void init(){
		filename = null;
		echo = true;
	}

	@Override
	public String getCommand() {
		return NAME;
	}

	/**
	 * For echo option : if echo|noecho is not provided , echo will be used.
	 */
	@Override
	public String getCommandUsage() {
		return USAGE;
	}

	@Override
	public List<String> getCommandOptions() {
		return OPTIONS;
	}

	@Override
	public int getCommandTokensLength() {
		return 3;
	}
	
	@Override
	protected Pattern getCommandRegex(){
		return Pattern.compile("(^input) (.*\\.sql)($| echo$| noecho$)");
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
		if(tokens.size() > 2){
			echo = OPTIONS.get(0).equals(tokens.get(2));
		}
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

}
