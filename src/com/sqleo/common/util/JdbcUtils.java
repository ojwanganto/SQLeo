/*
 *
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2013 anudeepgade@users.sourceforge.net
 *  
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
package com.sqleo.common.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;

public class JdbcUtils {

	public static void cancelAndCloseStatement(final Statement stmt) {
		if(stmt!=null)
		{	
			try {
				stmt.cancel();
			} catch (final SQLException e) {
				Application.println(e, false);
			}finally{
				try {
					if(stmt!=null){
						stmt.close();
					}
				} catch (SQLException e) {
					Application.println(e, false);
				}
			}
		}		
	}
	
	public static ResultSet executeQuery(final ConnectionHandler ch,final String sql, final Statement stmt) throws SQLException{
		// Ticket #337 - savepoint enable/disable preferences
		final boolean hasSavepoint = Preferences.getBoolean("application.autoSavePoint", false);
		final Savepoint savepoint;
		if (hasSavepoint){
			// Avoid PostgreSQL ERROR: current transaction is aborted
			final String savepointName = "AutoSavepoint";
			savepoint = ch.get().setSavepoint(savepointName);
		}else{
			savepoint = null;
		}
		try {
			return stmt.executeQuery(sql);
		} catch (SQLException sqle) {
			if(savepoint!=null){
				ch.get().rollback(savepoint);
			}
			throw sqle;
		}
	}

}
