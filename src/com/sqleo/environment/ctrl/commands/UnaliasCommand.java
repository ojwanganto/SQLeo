package com.sqleo.environment.ctrl.commands;

import java.util.List;
import java.util.regex.Pattern;

public class UnaliasCommand extends AbstractCommand {
	
	private static final String USAGE = "Usage: unalias <aliasname>, Description: Unalias user defined alias, syntax -> unalias selstar";
	public static String NAME = "unalias";

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
		//no options
		return null;
	}

	@Override
	public int getCommandTokensLength() {
		return 1;
	}
	
	@Override
	protected Pattern getCommandRegex() {
		return Pattern.compile("(^unalias)\\s(\\w.*)");
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
		if(AliasCommand.ALIAS_MAP.containsKey(aliasName)){
			AliasCommand.ALIAS_MAP.remove(aliasName);
			result.setCode(CommandExecutionResult.SUCCESS);
			return result;
		}else{
			return invalidCommandError(result, "Unknown alias provided ->" + aliasName);
		}

	}


}
