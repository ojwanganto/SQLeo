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

package com.sqleo.environment.ctrl.explorer;

import java.awt.Color;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;

public class UoDatasource
{
	private UoDriver uoDv;
	
	public String name	= new String("new database name");
	public String url	= new String();
	public String uid	= new String();
	public String pwd	= new String();
	
	/* tree filter */
	public String schema = null;
	
	public boolean remember = false;
	public boolean auto_connect = false;
	public boolean readonly = false;
	public String selectedFkDefFileName  = null;
	//default command editor background color is white
	public Color color = Color.white;
	
	public UoDatasource(UoDriver uoDv)
	{
		this.uoDv	= uoDv;
		this.url	= uoDv.example;
	}

	public void connect() throws Exception
	{
		ConnectionAssistant.open(uoDv.getKey(),this.getKey(),url,uid,pwd,selectedFkDefFileName,readonly);
	}

	public boolean isConnected()
	{
		return ConnectionAssistant.getHandler(this.getKey())!=null;
	}
	
	public void disconnect() throws Exception
	{
		ConnectionHandler ch = ConnectionAssistant.getHandler(this.getKey());
		ConnectionAssistant.removeHandler(this.getKey());
		
		ch.close();
	}
	
	public String getKey()
	{
		return uoDv.name +"."+ name +"@"+ (uid==null || uid.length() == 0 ?"<null>" : uid);
	}
	
	public void setSelectedFkDefFileName(String selectedFkDefFileName) {
		this.selectedFkDefFileName = selectedFkDefFileName;
		
	}

	public String getSelectedFkDefFileName() {
		return selectedFkDefFileName;
	}

	public String toString()
	{
		if(schema!=null)
			return name + " | " + schema;
		
		return name;
	}
}