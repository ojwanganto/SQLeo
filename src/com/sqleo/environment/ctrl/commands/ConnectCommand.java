package com.sqleo.environment.ctrl.commands;

import java.util.List;
import java.util.regex.Pattern;

import com.sqleo.environment.Application;
import com.sqleo.environment.mdi.ClientMetadataExplorer;

public class ConnectCommand extends AbstractCommand {

	private static final String USAGE =
		"Usage: connect <datasource>, Description: connects a datasource whose expected format is same value in the connection dropdown";
	public static String NAME = "connect";
	private ClientMetadataExplorer cme;

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
		// no-options
		return null;
	}

	@Override
	public int getCommandTokensLength() {
		return 2;
	}

	@Override
	protected Pattern getCommandRegex(){
		return Pattern.compile("(^connect)\\s(\\w.*)");
	}
	
	private ClientMetadataExplorer getMetadataExplorer(){
		cme = cme!=null ? cme : 
			(ClientMetadataExplorer)Application.window.getClient(ClientMetadataExplorer.DEFAULT_TITLE);
		return cme;
	}

	@Override
	public CommandExecutionResult execute(final String command) {
		final List<String> tokens = tokenizeCommand(command);
		if (tokens.isEmpty()) {
			return invalidCommandError(command);
		}
		final CommandExecutionResult result = new CommandExecutionResult();
		final String datasource = tokens.get(1);
		try{
			final boolean found = getMetadataExplorer().getControl().getNavigator().connect(datasource);
			if(found){
				result.setCode(CommandExecutionResult.SUCCESS);
			}else{
				result.setCode(CommandExecutionResult.FAILED);
				result.setDetail("No datasource found with: " + datasource);
			}
			return result;
		} catch (Exception e) {
			Application.println(e, false);
			result.setDetail("Unable to connect datasource: " + datasource +" due to "+e.getMessage());
			result.setCode(CommandExecutionResult.FAILED);
			return result;
		}
	}

}
