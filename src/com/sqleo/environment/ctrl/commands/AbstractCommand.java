package com.sqleo.environment.ctrl.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.sqleo.environment.Application;

public abstract class AbstractCommand implements Command {

	protected List<String> tokenizeCommand(final String command) {
		final StringTokenizer tokens = new StringTokenizer(command);
		final List<String> commandWithOptions = new ArrayList<String>();
		if (tokens.countTokens() < getCommandTokensLength()) {
			Application
					.alert("Invalid arguments received for command :" + command + ", see usage:" + getCommandUsage());
			return commandWithOptions;
		}
		while (tokens.hasMoreTokens()) {
			commandWithOptions.add(tokens.nextToken());
		}
		assert !commandWithOptions.isEmpty();
		assert getCommand().startsWith(commandWithOptions.get(0));
		return commandWithOptions;
	}
}
