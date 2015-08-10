package com.sqleo.environment.ctrl.commands;

import java.util.List;

public interface Command {

	/**
	 * Unique name representing a command 
	 * @return
	 */
	String getCommand();
	
	/**
	 * Example usage of a command
	 * @return
	 */
	String getCommandUsage();
	
	/**
	 * Options of a command
	 * @return
	 */
	List<String> getCommandOptions();
	
	
	/**
	 * Maximum number of tokens command can have including it
	 * @return
	 */
	int getCommandTokensLength();
	
	/**
	 * Execute command 
	 * @param command
	 * @return
	 */
	CommandExecutionResult execute(final String command);

}
