package com.sqleo.environment.ctrl.commands;

import java.util.Arrays;
import java.util.List;

import com.sqleo.environment.Application;

public class FormatCommand extends AbstractCommand {

	public static final String NAME = "format";
	private static final List<String> OPTIONS = Arrays.asList("csv", "delimiter", "header", "quote");
	private static final String USAGE =
			"Usage: format csv <delimiter ;> <header true> <quote true>, Description: CSV export format, default options for delimiter=; , header=false, quote=false when not provided";

	public String delimiter;
	public boolean header;
	public boolean quote;
	
	private void init(){
		delimiter = ";";
		header = false;
		quote = false;
	}

	@Override
	public String getCommand() {
		return NAME;
	}

	/**
	 * For csv option : delimiter ; , header false , quote false will be used by default if not provided
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

	private boolean validateToken(final int optIndex, final String token) {
		if (!OPTIONS.get(optIndex).equals(token)) {
			Application.alert("Given option: " + token + " is invalid, expected:" + OPTIONS.get(optIndex)
					+ " see usage" + USAGE);
			return false;
		}
		return true;
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
		if (!validateToken(0, tokens.get(1))) {
			result.setCode(CommandExecutionResult.INVALID);
			return result;
		}
		final int tokSize = tokens.size();
		if (tokSize >= 4) {
			if (!validateToken(1, tokens.get(2))) {
				result.setCode(CommandExecutionResult.INVALID);
				return result;
			}
			delimiter = tokens.get(3);
		}
		if (tokSize >= 6) {
			if (!validateToken(2, tokens.get(4))) {
				result.setCode(CommandExecutionResult.INVALID);
				return result;
			}
			header = Boolean.valueOf(tokens.get(5));
		}
		if (tokSize >= 8) {
			if (!validateToken(3, tokens.get(6))) {
				result.setCode(CommandExecutionResult.INVALID);
				return result;
			}
			quote = Boolean.valueOf(tokens.get(7));
		}
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

}
