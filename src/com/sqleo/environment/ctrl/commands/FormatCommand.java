package com.sqleo.environment.ctrl.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FormatCommand extends AbstractCommand {

	public static final String NAME = "format";
	private static final List<String> OPTIONS = Arrays.asList("csv", "delimiter", "header", "quote");
	private static final String USAGE =
			"Usage: format csv delimiter <char> header true|false quote true|false, Description: CSV export format, default options : delimiter ; header true quote false";

	public String delimiter;
	public boolean header;
	public boolean quote;
	
	private void init(){
		delimiter = ";";
		header = true;
		quote = false;
	}

	@Override
	public String getCommand() {
		return NAME;
	}

	/**
	 * For csv option : delimiter ; , header true , quote false will be used by default if not provided
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
		// return Pattern.compile("(^format)\\s(csv)\\s(delimiter)\\s(\\w.*)");
		return null;
	}

	private String validateToken(final int optIndex, final String token) {
		if (!OPTIONS.get(optIndex).equals(token)) {
			return("Given option: " + token + " is invalid, expected:" + OPTIONS.get(optIndex)
					+ " see \n" + USAGE);
		}
		return null;
	}

	@Override
	public CommandExecutionResult execute(String command) {
		init();
		final List<String> tokens = tokenizeCommand(command);
		if (tokens.isEmpty()) {
			return invalidCommandError(command);
		}
		final CommandExecutionResult result = new CommandExecutionResult();
		String errorToken = validateToken(0, tokens.get(1)); 
		if (errorToken!=null){
			return invalidCommandError(result, errorToken);
		}
		final int tokSize = tokens.size();
		if (tokSize >= 4) {
			errorToken = validateToken(1, tokens.get(2));
			if (errorToken!=null) {
				return invalidCommandError(result, errorToken);
			}
			delimiter = tokens.get(3);
		}
		if (tokSize >= 6) {
			errorToken = validateToken(2, tokens.get(4));
			if (errorToken!=null) {
				return invalidCommandError(result, errorToken);
			}
			header = Boolean.valueOf(tokens.get(5));
		}
		if (tokSize >= 8) {
			errorToken = validateToken(3, tokens.get(6));
			if (errorToken!=null) {
				return invalidCommandError(result, errorToken);
			}
			quote = Boolean.valueOf(tokens.get(7));
		}
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

}
