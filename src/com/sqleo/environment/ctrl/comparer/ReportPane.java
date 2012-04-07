/*
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

package com.sqleo.environment.ctrl.comparer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.define.TableMetaData;


public class ReportPane extends BorderLayoutPanel implements _Analyzer, Runnable
{
	private static final String TAB = "\t";
	private static final String WAIT = "...";
	private static final String INDENT = "\t> ";
	
	private JTextArea output;
	
	private _Starter starter;
	private Thread task;
	
	public ReportPane()
	{
		setComponentCenter(new JScrollPane(output=new JTextArea()));
		output.setTabSize(4);
		output.setEditable(false);
	}
	
	private boolean isAlive()
	{
		return task != null && task.isAlive();
	}
	
	public void onStopped()
	{
		printLn("[" + new Date(System.currentTimeMillis()) + "] STOPPED!");
		
		task.interrupt();
		task = null;
	}

	public void perform(_Starter starter)
	{
		this.starter = starter;
		
		task = new Thread(this);
		task.start();
	}
	
	private void print(String s)
	{
		output.append(s);
	}
	
	private void printLn(String s)
	{
		print(s + "\n");
	}

	public void run()
	{
		output.setText(null);
		printLn("[" + new Date(System.currentTimeMillis()) + "] STARTED!\n");
		
		ConnectionHandler ch1 = ConnectionAssistant.getHandler(starter.getKeyHandler1());
		ConnectionHandler ch2 = ConnectionAssistant.getHandler(starter.getKeyHandler2());
		
		try
		{
			String catalog1 = starter.getSchema1() == null ? null : ch1.get().getCatalog();
			String catalog2 = starter.getSchema2() == null ? null : ch2.get().getCatalog();
			
//			printLn("\n>>> load table objects");
			
			if(!isAlive()) return;
			ArrayList al1 = getTables(ch1, starter.getKeyHandler1(), catalog1, starter.getSchema1());
			
			if(!isAlive()) return;
			ArrayList al2 = getTables(ch2, starter.getKeyHandler2(), catalog2, starter.getSchema2());

//			printLn("\n>>> compare tables ");
			print("\n" + getConnectionInfo(starter.getKeyHandler1(),starter.getSchema1()) + " <==> ");
			printLn(getConnectionInfo(starter.getKeyHandler2(),starter.getSchema2()));
			if(!isAlive()) return;
			compareNames(al1,al2);
			
			if(starter.isViceVersa())
			{
//				printLn("\n>>> compare tables ");
				print("\n" + getConnectionInfo(starter.getKeyHandler2(),starter.getSchema2()) + " <==> ");
				printLn(getConnectionInfo(starter.getKeyHandler1(),starter.getSchema1()));
				if(!isAlive()) return;
				compareNames(al2,al1);
			}
			
			printLn("\n[" + new Date(System.currentTimeMillis()) + "] FINISHED!\n");
		}
		catch (SQLException e)
		{
			Application.println(e,true);
			print(e.toString());
		}
		finally
		{
			starter.onFinished();
		}
	}
	
	private String getConnectionInfo(String keycah, String schema)
	{
		return keycah + (schema!=null ? "( " + schema + " )" : "");
	}
	
	private ArrayList getTables(ConnectionHandler ch, String keycah, String catalog, String schema) throws SQLException
	{
		print("searching into: " + getConnectionInfo(keycah,schema) + WAIT);
		
		ArrayList al = new ArrayList();
		ResultSet rs = ch.get().getMetaData().getTables(catalog,schema,starter.getTablePattern(),new String[]{"TABLE"});
		while(this.isAlive() && rs.next())
		{
			al.add(new TableMetaData(keycah,rs.getString(2),rs.getString(3)));
		}
		rs.close();
		
		printLn("found " + al.size());
		return al;
	}
	
	private void compareNames(ArrayList alSource, ArrayList alTarget)
	{
		int notfounds = 0;
		for(int i=0; this.isAlive() && i<alSource.size(); i++)
		{
			TableMetaData tmdSource = (TableMetaData)alSource.get(i);
			TableMetaData tmdTarget = null;
			
			for(int j=0; this.isAlive() && j<alTarget.size(); j++)
			{
				tmdTarget = (TableMetaData)alTarget.get(j);
				if(tmdSource.getName().equalsIgnoreCase(tmdTarget.getName())) break;
				tmdTarget = null;
			}
			
			if(tmdTarget == null)
			{
				printLn(INDENT + "TABLE NOT FOUND: " + tmdSource.getName());
				alSource.remove(i--);
				notfounds++;
			}
			else
			{
				printLn(TAB + tmdSource.getName());
		
				if(starter.checkColumns())
				{
					if(!isAlive()) return;
					compareColumns(tmdSource,tmdTarget);
				}
				
				if(starter.checkPrimaryKeys())
				{
					if(!isAlive()) return;
					comparePrimaryKeys(tmdSource,tmdTarget);
				}
				
				if(starter.checkExportedKeys())
				{
					if(!isAlive()) return;
					compareExportedKeys(tmdSource,tmdTarget);
				}
				
				if(starter.checkImportedKeys())
				{
					if(!isAlive()) return;
					compareImportedKeys(tmdSource,tmdTarget);
				}
			}
		}
		printLn(notfounds + " TABLES NOT FOUND");
	}

	private void compareImportedKeys(TableMetaData tmdSource, TableMetaData tmdTarget)
	{
		ArrayList alSourceIK = tmdSource.getImportedKeys();
		ArrayList alTargetIK = tmdTarget.getImportedKeys();

		for(int j=0; this.isAlive() && j<alSourceIK.size(); j++)
		{
			String s = tmdSource.getImportedKeyProperty(j,TableMetaData.IDX_REL_PKTABLE_NAME);
			s = s + "." + tmdSource.getImportedKeyProperty(j,TableMetaData.IDX_REL_PKCOLUMN_NAME);
			
			boolean found = false;
			for(int z=0; this.isAlive() && !found && z<alTargetIK.size(); z++)
			{
				String t = tmdTarget.getImportedKeyProperty(z,TableMetaData.IDX_REL_PKTABLE_NAME);
				t = t + "." + tmdTarget.getImportedKeyProperty(z,TableMetaData.IDX_REL_PKCOLUMN_NAME);
				
				if(s.equalsIgnoreCase(t)) found = true;
			}
			
			if(!found)
			{
				printLn(TAB + INDENT + "RELATION NOT FOUND: " + s);
			}
		}
	}

	private void compareExportedKeys(TableMetaData tmdSource, TableMetaData tmdTarget)
	{
		ArrayList alSourceEK = tmdSource.getExportedKeys();
		ArrayList alTargetEK = tmdTarget.getExportedKeys();

		for(int j=0; this.isAlive() && j<alSourceEK.size(); j++)
		{
			String s = tmdSource.getExportedKeyProperty(j,TableMetaData.IDX_REL_FKTABLE_NAME);
			s = s + "." + tmdSource.getExportedKeyProperty(j,TableMetaData.IDX_REL_FKCOLUMN_NAME);
			
			boolean found = false;
			for(int z=0; this.isAlive() && !found && z<alTargetEK.size(); z++)
			{
				String t = tmdTarget.getExportedKeyProperty(z,TableMetaData.IDX_REL_FKTABLE_NAME);
				t = t + "." + tmdTarget.getExportedKeyProperty(z,TableMetaData.IDX_REL_FKCOLUMN_NAME);
				
				if(s.equalsIgnoreCase(t)) found = true;
			}
			
			if(!found)
			{
				printLn(TAB + INDENT + "RELATION NOT FOUND: " + s);
			}
		}
	}

	private void comparePrimaryKeys(TableMetaData tmdSource, TableMetaData tmdTarget)
	{
		ArrayList alSourcePK = tmdSource.getPrimaryKeys();
		ArrayList alTargetPK = tmdTarget.getPrimaryKeys();

		for(int j=0; this.isAlive() && j<alSourcePK.size(); j++)
		{
			String s = tmdSource.getPrimaryKeyProperty(j,TableMetaData.IDX_PK_COLUMN_NAME);
			
			boolean found = false;
			for(int z=0; this.isAlive() && !found && z<alTargetPK.size(); z++)
			{
				String t = tmdTarget.getPrimaryKeyProperty(z,TableMetaData.IDX_PK_COLUMN_NAME);
				if(s.equalsIgnoreCase(t)) found = true;
			}
			
			if(!found)
			{
				printLn(TAB + INDENT + "COLUMN IS NOT PRIMARY KEY: " + s);
			}
		}
	}
	
	private void compareColumns(TableMetaData tmdSource, TableMetaData tmdTarget)
	{
		ArrayList alSourceCols = tmdSource.getColumns();
		ArrayList alTargetCols = tmdTarget.getColumns();

		for(int j=0; this.isAlive() && j<alSourceCols.size(); j++)
		{
			String s = tmdSource.getColumnProperty(j,TableMetaData.IDX_COL_COLUMN_NAME);
			
			boolean found = false;
			for(int z=0; this.isAlive() && !found && z<alTargetCols.size(); z++)
			{
				String t = tmdTarget.getColumnProperty(z,TableMetaData.IDX_COL_COLUMN_NAME);
				if(s.equalsIgnoreCase(t)) found = true;
			}
			
			if(!found)
			{
				printLn(TAB + INDENT + "COLUMN NOT FOUND: " + s);
			}
		}
	}
}
