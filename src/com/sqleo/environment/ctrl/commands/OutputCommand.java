package com.sqleo.environment.ctrl.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.sqleo.environment.Application;

public class OutputCommand extends AbstractCommand {

	public static final String NAME = "output";
	private static final List<String> OPTIONS = Arrays.asList("text", "grid", "csv");
	private static final String USAGE =
			"Usage: output text|grid|csv <filename> append|replace , Description: Extract query output to text or grid or csv. default option is replace when not provided ";

	public String filename;
	public boolean append;
	public boolean gridMode;
	
	private void init(){
		filename = null;
		append = false;
		gridMode = false;
	}

	@Override
	public String getCommand() {
		return NAME;
	}

	/**
	 * For csv option : if filename is not provided, a temp file will be used . if append/replace is not provided ,
	 * replace will be used.
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
		return 2;
	}
	
	@Override
	protected Pattern getCommandRegex(){
		//TODO pattern
		//return Pattern.compile("(^output)(text|grid)|(csv\\s(\\w.*)(append)(\\w*))");
		return null;
	}

	@Override
	public CommandExecutionResult execute(String command) {
		init();
		final CommandExecutionResult result = new CommandExecutionResult();
		final List<String> tokens = tokenizeCommand(command);
		if (tokens.isEmpty()) {
			result.setCode(CommandExecutionResult.INVALID);
			return result;
		}
		final String option = tokens.get(1);
		if (!OPTIONS.contains(option)) {
			Application.alert("Given option : " + option + " is invalid, see \n" + USAGE);
			result.setCode(CommandExecutionResult.INVALID);
			return result;
		}
		if ("text".equals(option)) {
			result.setCode(CommandExecutionResult.SUCCESS);
			return result;
		} else if ("grid".equals(option)) {
			gridMode = true;
			result.setCode(CommandExecutionResult.SUCCESS);
			return result;
		} else if ("csv".equals(option)) {
			final int tokSize = tokens.size();
			if (tokSize == 3) {
				filename = tokens.get(2);
			} else if (tokSize == 4) {
				filename = tokens.get(2);
				append = "append".equals(tokens.get(3));
			}
			result.setCode(CommandExecutionResult.SUCCESS);
			return result;
		}
		result.setCode(CommandExecutionResult.FAILED);
		return result;
	}

}
