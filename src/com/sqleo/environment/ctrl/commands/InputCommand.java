package com.sqleo.environment.ctrl.commands;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import com.sqleo.environment.Application;
import com.sqleo.environment.io.FileStreamSQL;

public class InputCommand extends AbstractCommand {

	public static final String NAME = "input";
	private static final String USAGE =
			"Usage: input <filename.sql>, Description: Run the commands provided in input file";

	public String inputSql;
	
	private void init(){
		inputSql = null;
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
		final String filename = tokens.get(1);
		try {
			inputSql = FileStreamSQL.readSQL(filename);
			result.setCode(CommandExecutionResult.SUCCESS);
		} catch (IOException e) {
			Application.println(e, false);
			result.setDetail(e.getMessage());
			result.setCode(CommandExecutionResult.FAILED);
		}
		return result;
	}

}
