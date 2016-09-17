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

package com.sqleo.environment.ctrl.content;

import java.awt.Cursor;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.ContentPane;
import com.sqleo.environment.ctrl.define.TableMetaData;
import com.sqleo.environment.io.ManualDBMetaData;
import com.sqleo.environment.io.ManualTableMetaData;
import com.sqleo.environment.mdi.ClientContent;
import com.sqleo.querybuilder.QueryModel;
import com.sqleo.querybuilder.syntax.QuerySpecification;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;


public class JumpManager implements Runnable
{
	private ContentPane owner;

	private Hashtable h = new Hashtable();
	private Vector v = new Vector();
	
	public static void perform(ContentPane control)
	{
		JumpManager j = new JumpManager();
		j.owner = control;
		
		new Thread(j).start();
	}
	
	private boolean isCSVFKMode(String handlerKey,String schema,String tableName){
		ConnectionHandler ch = ConnectionAssistant.getHandler(handlerKey);
		String catalog = null;
		try {
			catalog = schema == null ? null: ch.get().getCatalog();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ManualDBMetaData md = ConnectionAssistant.getManualDBMetaData(handlerKey);
		return md!=null && md.getManualTableMetaData(catalog, schema, tableName)!=null;
	}
	
	public void run()
	{
		int row = owner.getView().getRow();
		int col = owner.getView().getColumn();
		
		QuerySpecification qs = owner.getQueryModel().getQueryExpression().getQuerySpecification();
		if(!(qs.getSelectList()[col] instanceof QueryTokens.Column)) return;
		QueryTokens.Column csource = (QueryTokens.Column)qs.getSelectList()[col];
		QueryTokens.Table tsource = csource.getTable() != null ? csource.getTable() : (QueryTokens.Table)qs.getFromClause()[0];
		
		owner.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		TableMetaData tmd = new TableMetaData(owner.getHandlerKey(),tsource.getSchema(),tsource.getName());
		boolean isCSVMode = isCSVFKMode(owner.getHandlerKey(),tsource.getSchema(),tsource.getName());
		for(int i=0; i<tmd.getImportedKeys().size(); i++)
		{
			if(tmd.getImportedKeyProperty(i,TableMetaData.IDX_REL_FKCOLUMN_NAME)
// ticket #331 Content window: jump doesn't work (for lower case column names)
//					.equals(isCSVMode?csource.getName():csource.getName().toUpperCase()))
					.equals(isCSVMode?csource.getName():csource.getName()))
			{
				TableMetaData tmdPK = new TableMetaData(tmd.getHandlerKey(),
														tmd.getImportedKeyProperty(i,TableMetaData.IDX_REL_PKTABLE_SCHEM),
														tmd.getImportedKeyProperty(i,TableMetaData.IDX_REL_PKTABLE_NAME));
				
				String pk = tmd.getImportedKeyProperty(i,TableMetaData.IDX_REL_PKCOLUMN_NAME);
				String id = ">> " + tmdPK + " (" + pk + ")";

				h.put(id,tmdPK);

				if(!v.contains(id))
					v.addElement(id);
			}
		}
		
		if(isCSVMode || tmd.isPrimaryKey(csource.getName()))
		{
			for(int i=0; i<tmd.getExportedKeys().size(); i++)
			{
				if(tmd.getExportedKeyProperty(i,TableMetaData.IDX_REL_PKCOLUMN_NAME).
// ticket #331 Content window: jump doesn't work (for lower case column names)
//						equals(isCSVMode?csource.getName():csource.getName().toUpperCase()))
						equals(isCSVMode?csource.getName():csource.getName()))
				{
					TableMetaData tmdFK = new TableMetaData(tmd.getHandlerKey(),
															tmd.getExportedKeyProperty(i,TableMetaData.IDX_REL_FKTABLE_SCHEM),
															tmd.getExportedKeyProperty(i,TableMetaData.IDX_REL_FKTABLE_NAME));
					
					String fk = tmd.getExportedKeyProperty(i,TableMetaData.IDX_REL_FKCOLUMN_NAME);
					String id = "<< " + tmdFK + " (" + fk + ")";
					
					h.put(id,tmdFK);

					if(!v.contains(id))
						v.addElement(id);
				}	
			}
		}
		
		owner.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		Object jumpTo = null;
		if(v.size() == 1)
		{
			if(JOptionPane.showConfirmDialog(Application.window,"Jump from '" + csource + "' to:\n" + v.elementAt(0),Application.PROGRAM,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				jumpTo = v.elementAt(0);
		}
		else if(v.size() > 1)
		{
			jumpTo = JOptionPane.showInputDialog(Application.window,"Jump from '" + csource + "' to:",Application.PROGRAM,JOptionPane.PLAIN_MESSAGE,null,v.toArray(),null);
		}
		else
		{
			JOptionPane.showMessageDialog(Application.window,"Column '" + csource + "' has no references",Application.PROGRAM,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		if(jumpTo!=null)
		{
			owner.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			TableMetaData tmdJ = (TableMetaData)h.get(jumpTo);
			
			int pos = jumpTo.toString().indexOf('(');
			String filter = jumpTo.toString().substring(pos+1,jumpTo.toString().length()-1);
			
			int sqltype = 0;
			for(int i=0; i<tmdJ.getColumns().size(); i++)
			{
				if(tmdJ.getColumnProperty(i,TableMetaData.IDX_COL_COLUMN_NAME).equals(filter))
					sqltype = Integer.valueOf(tmdJ.getColumnProperty(i,TableMetaData.IDX_COL_DATA_TYPE)).intValue();
			}

			Object value = owner.getView().getValueAt(row,col);
			QueryTokens.Condition condition = new QueryTokens.Condition(new QueryTokens.Column(null,filter),"=",new QueryTokens.DefaultExpression(SQLFormatter.toJdbcValue(value,sqltype)));
			
			QueryModel qm = tmdJ.createQueryModel();
			qm.getQueryExpression().getQuerySpecification().addWhereClause(condition);
			
			ClientContent client = new ClientContent(tmdJ.getHandlerKey(),qm,null);
			client.setTitle("CONTENT : " + tmdJ + " : " + tmdJ.getHandlerKey());			
			Application.window.add(client);
			
			owner.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
}