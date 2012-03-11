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

package nickyb.sqleonardo.environment.ctrl.content;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import nickyb.sqleonardo.common.jdbc.ConnectionAssistant;
import nickyb.sqleonardo.common.jdbc.ConnectionHandler;
import nickyb.sqleonardo.environment.Application;
import nickyb.sqleonardo.environment.ctrl.define.TableMetaData;
import nickyb.sqleonardo.querybuilder.syntax.SQLFormatter;

public abstract class AbstractActionContent extends AbstractAction
{	
	protected abstract void onActionPerformed(int records, int option);
	protected abstract TableMetaData getTableMetaData();
	
	protected final String getDefaultMessage(int records)
	{
		return this.getTableMetaData().getType() + " : '" + this.getTableMetaData() + "' contains " + NumberFormat.getInstance().format(records) + " record(s)";
	}

	protected int showConfirmDialog(int records)
	{
		String message = this.getDefaultMessage(records) + "\ndo you want continue?";
		return JOptionPane.showConfirmDialog(Application.window,message,this.getValue(NAME).toString(),JOptionPane.YES_NO_OPTION);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(this.getTableMetaData() == null
		|| this.getTableMetaData().getHandlerKey() == null) return;
		
		try
		{
			ConnectionHandler ch = ConnectionAssistant.getHandler(this.getTableMetaData().getHandlerKey());
			String identifierQuoteString = ConnectionAssistant.getHandler(this.getTableMetaData().getHandlerKey()).getObject("$identifierQuoteString").toString();
			
			Statement stmt = ch.get().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + SQLFormatter.ensureQuotes(this.getTableMetaData().toString(),identifierQuoteString,true));
			
			int records = rs.next() ? rs.getInt(1) : 0;
			
			rs.close();
			stmt.close();
			
			onActionPerformed(records,showConfirmDialog(records));
		}
		catch(SQLException sqle)
		{
			Application.println(sqle,true);
		}
	}
}