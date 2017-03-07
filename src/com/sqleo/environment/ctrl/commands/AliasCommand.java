package com.sqleo.environment.ctrl.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AliasCommand extends AbstractCommand {
	
	private static final String USAGE = "Usage: alias, Description: Syntax -> alias selstar=\"select * from ?\" , @selstar mytable;  ";
	public static String NAME = "alias";
	
	public static Map<String,String> ALIAS_MAP = new HashMap<String,String>();

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
	protected Pattern getCommandRegex() {
		//alias selstar="select * from ?"
		return Pattern.compile("(^alias)\\s(\\w.*)=\\\"(\\w.*)\\\"");
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
		final String aliasName = tokens.get(1);
		final String aliasValue = tokens.get(2);
		ALIAS_MAP.put(aliasName, aliasValue);
		
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

	

}
