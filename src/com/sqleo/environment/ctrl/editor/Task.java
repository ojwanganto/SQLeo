/*
 *
 * Modified by SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
 * SQLeonardo :: java database frontend
 * Copyright (C) 2004 nickyb@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.sqleo.environment.ctrl.editor;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.text.NumberFormat;
import java.util.Arrays;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.JdbcUtils;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;



public class Task implements Runnable {
	private static final int MAX_DISPLAY_SIZE = 50;

	private _TaskSource source;
	private _TaskTarget target;

	private int limit;

	private Statement stmt = null;
	private ResultSet rs = null;
	private static final int TIMESTAMP_WITH_TIMEZONE = -101;

	public Task(_TaskSource source, _TaskTarget target, int limit) {
		this.source = source;
		this.target = target;
		this.limit = limit;
	}

	@Override
	public void run() {
		String message = null;

		try {
			String syntax = source.getSyntax().trim();
			if(syntax.isEmpty()){
				return;
			}
			if (ConnectionAssistant.hasHandler(source.getHandlerKey())) {
				if (syntax.length() >= 4) {
					ConnectionHandler ch = ConnectionAssistant
							.getHandler(source.getHandlerKey());
					stmt = ch.get().createStatement();
					stmt.setFetchSize(1000);
					stmt.setMaxRows(limit);
					long started = System.nanoTime();

					syntax = SQLHelper.getSQLeoFunctionQuery(syntax,source.getHandlerKey());

					String sqlcmd = syntax.length() > 7 ?  syntax.toUpperCase().substring(0, 7) : syntax.toUpperCase();
					if (sqlcmd.startsWith("WITH") || sqlcmd.startsWith("SHOW")) {
						rs = stmt.executeQuery(syntax);
						printSelect();
						if(rs!=null){
							rs.close();
						}
					}else if (sqlcmd.startsWith("SELECT")) {
						// test #329 Query builder / Command editor: avoid PostgreSQL ERROR: current transaction is aborted
						try {
							rs = JdbcUtils.executeQuery(ch, syntax, stmt);
						} catch (SQLException sqle) {
							target.onTaskFinished(sqle.toString(), true);
						}
						if(target.printSelect()){
							printSelect();
						}else{
							target.processResult(rs);
						}
						if(rs!=null){
							rs.close();
						}
					} else if (sqlcmd.startsWith("DECLARE") || sqlcmd.startsWith("BEGIN") || sqlcmd.startsWith("DO")) {

						// dbms output enable
						CallableStatement stmtOutput = ch.get().prepareCall("{call dbms_output.enable(1000000) }");
						boolean dbmsOutput = true;  
						String savepointName = "AutoSavepoint";
						Savepoint savepoint= ch.get().setSavepoint(savepointName);
						try {
							stmtOutput.execute();
						} catch (SQLException sqle) {
							ch.get().rollback(savepoint);
							dbmsOutput = false;  // dbms_output is not available by default in postgres
						}

						// execute current statement
						stmt.executeUpdate(syntax);
						target.write("\n");

						// dbms output get
						if(dbmsOutput) {
							stmtOutput =ch.get().prepareCall("{call dbms_output.get_line(?,?)}");
							stmtOutput.registerOutParameter(1,java.sql.Types.VARCHAR);
							stmtOutput.registerOutParameter(2,java.sql.Types.INTEGER);
						        for (int i=0;;i++){
        							stmtOutput.execute();
						        	if(stmtOutput.getInt(2)== 1){
					        			break;
					        		}
					        		target.write(stmtOutput.getString(1) + "\n");                
					        	}

							// dbms output disable
							stmtOutput = ch.get().prepareCall("{call dbms_output.disable() }");
							stmtOutput.execute();
						}
						target.write("PL/SQL block executed successfully");
					        stmtOutput.close();

					} else {
						rs = null;
						// Ticket #337 - savepoint enable/disable preferences
						boolean hasSavepoint = Preferences.getBoolean("application.autoSavePoint", false);
						final Savepoint savepoint;
						if (hasSavepoint){
							// test #329 Query builder / Command editor: avoid PostgreSQL ERROR: current transaction is aborted
							String savepointName = "AutoSavepoint";
							savepoint= ch.get().setSavepoint(savepointName);
						}else{
							savepoint = null;
						}
						try {
							  	int rows = stmt.executeUpdate(syntax);
								if (sqlcmd.startsWith("DELETE")
										|| sqlcmd.startsWith("INSERT")
										|| sqlcmd.startsWith("UPDATE")) {
									target.write(rows + " row(s) affected");
								} else {
									target.write("Command has been executed successfully");
								}
							} catch (SQLException sqle) {
								if(savepoint!=null){
									ch.get().rollback(savepoint);
								}
								target.onTaskFinished(sqle.toString(), true);
							}
						// end test #329
					}
// already closed ???
//					stmt.close();

					long ended = System.nanoTime();
					long nano = ended - started;	
					double seconds = (double) nano / (double) 1000000000;

					target.write( " [ seconds: " + NumberFormat.getInstance().format(seconds) + " ]");


					if (!target.continueRun()) {
						message = "Execution has been stopped";
					}
				}
			} else {
				message = "No connection!";
			}

			target.onTaskFinished(message, false);
		} catch (SQLException sqle) {
			target.onTaskFinished(sqle.toString(), true);
		}
	}
	
	public void cancel() 
	{
		if(stmt!=null){
			try {
				stmt.cancel();
				stmt.close();
				stmt = null;
			} catch (Exception e) {
				Application.println(e, true);
			}
		}
	}

	private void printSelect() throws SQLException {
		if (rs == null || !target.printSelect()) {
			return;
		}

		int maxSize = Preferences.getInteger("editor.maxcolsize",
				MAX_DISPLAY_SIZE);

		// System.out.println("reading...");

		StringBuffer header = new StringBuffer("| ");
		StringBuffer divider = new StringBuffer("+-");

		int columnDisplaySize[] = new int[getColumnCount()];
		for (int i = 1; i <= getColumnCount(); i++) {
			header.append(getColumnLabel(i));

			char[] filler = new char[getColumnLabel(i).length()];
			Arrays.fill(filler, '-');
			divider.append(filler);

			columnDisplaySize[i - 1] = getColumnDisplaySize(i);
			int diff = columnDisplaySize[i - 1] - getColumnLabel(i).length();

			if (diff > 0) {
				if (diff > maxSize) {
					diff = maxSize - getColumnLabel(i).length();
					columnDisplaySize[i - 1] = maxSize;
				}

				filler = new char[diff];
				Arrays.fill(filler, ' ');
				header.append(filler);

				Arrays.fill(filler, '-');
				divider.append(filler);
			} else {
				columnDisplaySize[i - 1] = getColumnLabel(i).length();
			}

			header.append(" | ");
			divider.append("-+-");
		}

		divider.deleteCharAt(divider.length() - 1);
		header.deleteCharAt(header.length() - 1);

		target.write(divider.toString() + "\n");
		target.write(header.toString() + "\n");
		target.write(divider.toString() + "\n");

		int bytes = 0;
		int rowcount = 0;
		while (rs.next() && target.continueRun()) {
			rowcount++;
			// System.out.println("reading record " + rowcount + " , bytes " +
			// bytes);

			StringBuffer row = new StringBuffer("| ");

			for (int i = 1; i <= getColumnCount(); i++) {
				String value = SQLHelper.getRowValue(rs, i);
				if(null == value) {
					value = new String();
				}

				bytes += value.length();

				int diff = columnDisplaySize[i - 1] - value.length();
				if (diff > 0) {
					char[] filler = new char[diff];
					Arrays.fill(filler, ' ');
					row.append(value + new String(filler));
				} else if (diff < 0) {
					value = value.substring(0, columnDisplaySize[i - 1] - 3);
					row.append(value + "...");
				} else {
					row.append(value);
				}

				row.append(" | ");
			}
			row.deleteCharAt(row.length() - 1);
			target.write(row.toString() + "\n");
		}

		if (rowcount > 0) {
			target.write(divider.toString() + "\n");
		}
		target.write("Record(s): " + NumberFormat.getInstance().format(rowcount) 
				+ " [ bytes: " + NumberFormat.getInstance().format(bytes) + " ]");
	}

	public int getColumnCount() throws SQLException {
		return rs.getMetaData().getColumnCount();
	}

	public int getColumnDisplaySize(int index) throws SQLException {
		int type = rs.getMetaData().getColumnType(index);
		if (type == Types.TIMESTAMP || type == TIMESTAMP_WITH_TIMEZONE || type == Types.DATE || type == Types.TIME) {
			return 35;
		}

		return rs.getMetaData().getColumnDisplaySize(index);
	}

	public String getColumnLabel(int index) throws SQLException {
		return rs.getMetaData().getColumnLabel(index);
	}
}
