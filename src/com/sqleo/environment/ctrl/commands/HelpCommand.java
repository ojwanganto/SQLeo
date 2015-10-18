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
		if (tokens.isEmpty()) {
			return invalidCommandError(command);
		}
		final String help = tokens.get(0);
		final CommandExecutionResult result = new CommandExecutionResult();
		if (!help.equals(NAME)) {
			return invalidCommandError(result, "Given option: " + help + " is invalid, expected:" + NAME + " see \n" + USAGE);
		}
		final StringBuilder builder = new StringBuilder();
		builder.append("List of available commands with usage");
		int i = 1;
		for (final Command cmd : Application.commandRunner.getCommands()) {
			builder.append("\n").append(i).append(". ").append(String.format("%-8s", cmd.getCommand())).append("\t")
					.append(cmd.getCommandUsage());
			i++;
		}
		//add other commands
		i=1;
		builder.append("\n\nList of main shortcuts (Full list available in Help menu)");

		builder.append("\n").append(i).append(". ").append(String.format("%-8s", "CTRL-ENTER")).append("\t")
		.append("Execute current or previous query");
		i++;
		builder.append("\n").append(i).append(". ").append(String.format("%-8s", "CTRL-T")).append("\t\t")
		.append("Clear request area");
		i++;
		builder.append("\n").append(i).append(". ").append(String.format("%-8s", "CTRL-B")).append("\t")
		.append("Clear response area");
		i++;
		builder.append("\n").append(i).append(". ").append(String.format("%-8s", "CTRL-F7")).append("\t")
		.append("Format selected text");
		i++;
		builder.append("\n").append(i).append(". ").append(String.format("%-8s", "CTRL-SPACE")).append("\t")
		.append("Auto-join completion");
		i++;
		
		builder.append("\n");
		result.setDetail(builder.toString());
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

}
