package com.sqleo.environment.ctrl.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class AliasListCommand extends AbstractCommand {

	private static final String USAGE = "Usage: aliaslist, Description: Shows all user defined aliases";
	public static String NAME = "aliaslist";

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
		return Pattern.compile("(^aliaslist)");
	}

	@Override
	public CommandExecutionResult execute(String command) {
		final List<String> tokens = tokenizeCommand(command);
		if (tokens.isEmpty()) {
			return invalidCommandError(command);
		}
		final String aliaslist = tokens.get(0);
		final CommandExecutionResult result = new CommandExecutionResult();
		if (!aliaslist.equals(NAME)) {
			return invalidCommandError(result, "Given option: " + aliaslist + " is invalid, expected:" + NAME + " see \n" + USAGE);
		}
		final StringBuilder builder = new StringBuilder();
		builder.append("List of available aliases");
		int i = 1;
		final List<String> aliases = new ArrayList<String>(AliasCommand.ALIAS_MAP.keySet());
		Collections.sort(aliases);
		for(final String alias : aliases){	
			builder.append("\n").append(i).append(". ").append(alias);
			i++;
		}
		builder.append("\n");
		result.setDetail(builder.toString());
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

}
