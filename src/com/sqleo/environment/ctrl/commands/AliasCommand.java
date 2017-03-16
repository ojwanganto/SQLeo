package com.sqleo.environment.ctrl.commands;

import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.sqleo.environment.Application;

public class AliasCommand extends AbstractCommand {
	
	private static final String USAGE = "Usage: alias <aliasname>=<query>, Description: Alias any query with parameters Example: alias selstar=\"select * from ?\" , @selstar mytable;  ";
	public static String NAME = "alias";
	
	public static Hashtable<String,String> ALIAS_MAP;
	
	public static void loadAliases(){
		if(Application.session.mount(Application.ENTRY_ALIASES).size() == 0)
		{
			Application.session.mount(Application.ENTRY_ALIASES).add(new Hashtable<String,String>());
		}
		ALIAS_MAP = (Hashtable)Application.session.mount(Application.ENTRY_ALIASES).get(0);
		
	}
	
	private static String REPLACE_MATCH = "\\?";

	public static String getAliasedSQL(final String sql){
		if(sql.charAt(0) == '@'){
			final String sqlSuffix = sql.substring(1); 
			final StringTokenizer sqlTokens = new StringTokenizer(sqlSuffix);
			final String alias = sqlTokens.nextToken();
			if(alias!=null){
				final String aliasVal = ALIAS_MAP.get(alias);
				if(aliasVal!=null){
					String sqlReplaced = aliasVal;
					while(sqlTokens.hasMoreTokens()){
						sqlReplaced = sqlReplaced.replaceFirst(REPLACE_MATCH, sqlTokens.nextToken());
					}
					return sqlReplaced;
				}
			}
		}
		return null;
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
		return 1;
	}
	
	@Override
	protected Pattern getCommandRegex() {
		//alias selstar="select * from ?"
		return Pattern.compile("(^alias)\\s(\\w.*)=\\\"(.*)\\\"", Pattern.DOTALL);
	}

	@Override
	public CommandExecutionResult execute(String command) {
		final List<String> tokens = tokenizeCommand(command);
		if (tokens.isEmpty()) {
			return invalidCommandError(command);
		}
		final String givenCmd = tokens.get(0);
		final CommandExecutionResult result = new CommandExecutionResult();
		if (!givenCmd.equals(NAME)) {
			return invalidCommandError(result, "Given option: " + givenCmd + " is invalid, expected:" + NAME + " see \n" + USAGE);
		}
		final String aliasName = tokens.get(1);
		final String aliasValue = tokens.get(2);
		ALIAS_MAP.put(aliasName, aliasValue);
		
		result.setCode(CommandExecutionResult.SUCCESS);
		return result;
	}

	

}
