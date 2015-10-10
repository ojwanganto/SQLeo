package com.sqleo.environment.ctrl.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class OutputCommand extends AbstractCommand {

	private static final Pattern OUTPUT_CMD_PATTERN = Pattern.compile("(?:(output) (text|grid))|(?:(output) (csv) (.*) (append|replace))");
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
	 * For csv option : if append/replace is not provided , replace will be used.
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
		return OUTPUT_CMD_PATTERN;
	}

	@Override
	public CommandExecutionResult execute(String command) {
		init();
		final List<String> tokens = tokenizeCommand(command);
		if (tokens.isEmpty()) {
			return invalidCommandError(command);
		}
		final String option = tokens.get(1);
		final CommandExecutionResult result = new CommandExecutionResult();
		if (!OPTIONS.contains(option)) {
			return invalidCommandError(result, "Given option : " + option + " is invalid, see \n" + USAGE);
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
