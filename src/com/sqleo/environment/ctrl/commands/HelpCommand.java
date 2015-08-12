package com.sqleo.environment.ctrl.commands;

import java.util.List;
import java.util.regex.Pattern;

import com.sqleo.environment.Application;

public class HelpCommand extends AbstractCommand {

	private static final String USAGE = "Usage: help, Description: Shows all available commands";
	public static String NAME = "help";

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
		return Pattern.compile("(^help)");
	}

	@Override
	public CommandExecutionResult execute(String command) {
		final List<String> tokens = tokenizeCommand(command);
		final CommandExecutionResult result = new CommandExecutionResult();
		if (tokens.isEmpty()) {
			result.setCode(CommandExecutionResult.INVALID);
			return result;
		}
		final String help = tokens.get(0);
		if (!help.equals(NAME)) {
			Application.alert("Given option: " + help + " is invalid, expected:" + NAME + " see usage" + USAGE);
			result.setCode(CommandExecutionResult.INVALID);
			return result;
		}
		final StringBuilder builder = new StringBuilder();
		builder.append("List of available commands with usage");
		int i = 1;
		for (final Command cmd : Application.commandRunner.getCommands()) {
			builder.append("\n").append(i).append(". ").append(String.format("%-8s", cmd.getCommand())).append("\t")
					.append(cmd.getCommandUsage());
			i++;
		}
		result.setDetail(builder.toString());
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

}
