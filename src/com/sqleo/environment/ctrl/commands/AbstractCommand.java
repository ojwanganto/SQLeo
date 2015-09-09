package com.sqleo.environment.ctrl.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sqleo.common.util.Text;

public abstract class AbstractCommand implements Command {

	protected  abstract Pattern getCommandRegex();
	
	protected List<String> tokenizeCommand(final String command) {
		if(getCommandRegex()!=null){
			return tokenizeCommandWithRegex(command);
		}else{
			return tokenizeCommandWithTokenizer(command);
		}
	}
	
	protected List<String> tokenizeCommandWithTokenizer(final String command) {
		final StringTokenizer tokens = new StringTokenizer(command);
		final List<String> commandWithOptions = new ArrayList<String>();
		if (tokens.countTokens() < getCommandTokensLength()) {
			return commandWithOptions;
		}
		while (tokens.hasMoreTokens()) {
			commandWithOptions.add(tokens.nextToken());
		}
		assert !commandWithOptions.isEmpty();
		assert getCommand().startsWith(commandWithOptions.get(0));
		return commandWithOptions;
	}
	
	protected List<String> tokenizeCommandWithRegex(final String command) {
		final Matcher matcher = getCommandRegex().matcher(command);
		final List<String> commandWithOptions = new ArrayList<String>();
		if (matcher.groupCount() < getCommandTokensLength()) {
			return commandWithOptions;
		}
		while (matcher.find()) {
			for(int i = 1 ; i <= matcher.groupCount() ; i++) {
				final String match =  matcher.group(i);
				if(match!=null && !match.isEmpty()){
					commandWithOptions.add(Text.trimBoth(match));
				}
			}
		}
		assert !commandWithOptions.isEmpty();
		assert getCommand().startsWith(commandWithOptions.get(0));
		return commandWithOptions;
	}
	
	protected CommandExecutionResult invalidCommandError(final String command){
		final CommandExecutionResult result = new CommandExecutionResult();
		return invalidCommandError(result, "Invalid arguments received for command :" + command + ", see usage:" + getCommandUsage());
	}
	
	protected CommandExecutionResult invalidCommandError(final CommandExecutionResult result,final String detail){
		result.setDetail(detail);
		result.setCode(CommandExecutionResult.INVALID);
		return result;
	}

	
}
