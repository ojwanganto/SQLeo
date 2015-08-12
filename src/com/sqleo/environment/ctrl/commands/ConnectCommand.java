package com.sqleo.environment.ctrl.commands;

import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.explorer.UoDatasource;
import com.sqleo.environment.ctrl.explorer.UoDriver;

public class ConnectCommand extends AbstractCommand {

	private static final String USAGE =
		"Usage: connect <datasource>, Description: connects a datasource whose expected format is same value in the connection dropdown";
	public static String NAME = "connect";

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

	@Override
	public CommandExecutionResult execute(final String command) {
		final List<String> tokens = tokenizeCommand(command);
		final CommandExecutionResult result = new CommandExecutionResult();
		if (tokens.isEmpty()) {
			result.setCode(CommandExecutionResult.INVALID);
			return result;
		}
		final String datasource = tokens.get(1);
		if (Application.session.canMount(Application.ENTRY_JDBC)) {
			Application.session.mount(Application.ENTRY_JDBC);
			Application.session.home();
			for (Enumeration eDv = Application.session.jumps(); eDv.hasMoreElements();) {
				final String driverName = eDv.nextElement().toString();
				Application.session.jump(driverName);

				final UoDriver uoDv = new UoDriver();
				uoDv.name = driverName;
				final Object[] dvInfo = (Object[])Application.session.jump().get(0);
				uoDv.library	= dvInfo[0] == null ? "" : dvInfo[0].toString();
				uoDv.classname	= dvInfo[1] == null ? "" : dvInfo[1].toString();

				for (Enumeration eDs = Application.session.jumps(); eDs.hasMoreElements();) {
					final String dsName = eDs.nextElement().toString();
					Application.session.home();
					Application.session.jump(new String[] { driverName, dsName });
					final Object[] dsInfo = (Object[]) Application.session.jump().get(0);
					final String url = dsInfo[0] == null ? "" : dsInfo[0].toString();
					final String uid = dsInfo[1] == null ? "" : dsInfo[1].toString();
					final String pwd = dsInfo[2] == null ? "" : dsInfo[2].toString();

					final UoDatasource uoDs = new UoDatasource(uoDv);
					uoDs.name = dsName;
					uoDs.uid = uid;
					uoDs.url = url;
					uoDs.pwd = pwd;

					if(uoDs.getKey().equals(datasource)){
						final boolean isAutoBefore = Preferences.isAutoSelectConnectionEnabled();
						try{
							if(!uoDs.isConnected()){
								try {
									uoDs.connect();
									Application.window.connectionOpened(uoDs.getKey());
									result.setCode(CommandExecutionResult.SUCCESS);
									return result;
								} catch (Exception e) {
									Application.println(e,true);
									result.setCode(CommandExecutionResult.FAILED);
									return result;
								}
							}else{
								Application.window.connectionOpened(uoDs.getKey());
								result.setCode(CommandExecutionResult.SUCCESS);
								return result;
							}
						}finally{

						}
					}
				}
				Application.session.home();
			}
		}
		Application.alert("No datasource found with :" + datasource);
		result.setCode(CommandExecutionResult.FAILED);
		return result;
	}

}
