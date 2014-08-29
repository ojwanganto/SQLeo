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

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.environment.ctrl.MetadataExplorer;

public class UoDriver
{
	public String name		= new String("new driver name");
	public String library	= null;
	public String classname	= new String();
	public String example	= new String();
	public String message	= null;
	
	public String getKey()
	{
		return library +"$"+ classname;
	}
	
	public String toString()
	{
		return name;
	}
	
	public static void loadDefaults(MetadataExplorer explorer)
	{
		UoDriver[] drivers = new UoDriver[14];

		drivers[0] = new UoDriver();
		drivers[0].name			= "ODBC Bridge";
		drivers[0].classname	= "sun.jdbc.odbc.JdbcOdbcDriver";
		drivers[0].example		= "jdbc:odbc:<data source name>";
		
		drivers[1] = new UoDriver();
		drivers[1].name			= "Apache Derby";
		drivers[1].classname	= "org.apache.derby.jdbc.ClientDriver";
		drivers[1].example		= "jdbc:derby:net://<host>:<port1527>/<databaseName>";

		drivers[2] = new UoDriver();
		drivers[2].name			= "HSQLDB - Embedded";
		drivers[2].classname	= "org.hsqldb.jdbcDriver";
		drivers[2].example		= "jdbc:hsqldb:<database>";

		drivers[3] = new UoDriver();
		drivers[3].name			= "HSQLDB - Server";
		drivers[3].classname	= "org.hsqldb.jdbcDriver";
		drivers[3].example		= "jdbc:hsqldb:hsql://<host>:<port>";

		drivers[4] = new UoDriver();
		drivers[4].name			= "jTDS - SQL Server";
		drivers[4].classname	= "net.sourceforge.jtds.jdbc.Driver";
		drivers[4].example		= "jdbc:jtds:sqlserver://<server>[:<port>][/<database>]";

		drivers[5] = new UoDriver();
		drivers[5].name			= "jTDS - Sybase";
		drivers[5].classname	= "net.sourceforge.jtds.jdbc.Driver";
		drivers[5].example		= "jdbc:jtds:sybase://<server>[:<port>][/<database>]";

		drivers[6] = new UoDriver();
		drivers[6].name			= "MySQL";
		drivers[6].classname	= "com.mysql.jdbc.Driver";
		drivers[6].example		= "jdbc:mysql://<host>:<port3306>/<database>";

		drivers[7] = new UoDriver();
		drivers[7].name			= "Oracle Thin";
		drivers[7].classname	= "oracle.jdbc.OracleDriver";
		drivers[7].example		= "jdbc:oracle:thin:@<host>:<port1521>:<SID>";

		drivers[8] = new UoDriver();
		drivers[8].name			= "Oracle OCI";
		drivers[8].classname	= "oracle.jdbc.OracleDriver";
		drivers[8].example		= "jdbc:oracle:oci:@<host>:<port1521>:<SID>";

		drivers[9] = new UoDriver();
		drivers[9].name			= "PostgreSQL";
		drivers[9].classname	= "org.postgresql.Driver";
		drivers[9].example		= "jdbc:postgresql://<host>:<port5432>/<database>";
		
		drivers[10] = new UoDriver();
		drivers[10].name		= "Firebird";
		drivers[10].classname	= "org.firebirdsql.jdbc.FBDriver";
		drivers[10].example		= "jdbc:firebirdsql://<host>:<port3050>/<database>";
				
		drivers[11] = new UoDriver();
		drivers[11].name		= "CsvJdbc";
		drivers[11].classname	= "org.relique.jdbc.csv.CsvDriver";
		drivers[11].example		= "jdbc:relique:csv:<directory_path>?separator=;";

		drivers[12] = new UoDriver();
		drivers[12].name		= "Apache Derby - Embedded";
		drivers[12].classname	= "org.apache.derby.jdbc.EmbeddedDriver";
		drivers[12].example		= "jdbc:derby:<derbyDB>;create=true";

		drivers[13] = new UoDriver();
		drivers[13].name			= "MySQL (MariaDB Jdbc Driver)";
		drivers[13].classname	= "org.mariadb.jdbc.Driver";
		drivers[13].example		= "jdbc:mysql://<host>:<port3306>";

		for(int i=0; i<drivers.length; i++)
		{
			try
			{
				ConnectionAssistant.declare(null,drivers[i].classname,true);
			}
			catch (Exception e)
			{
				drivers[i].message = e.toString();
			}
			finally
			{
				explorer.getNavigator().add(drivers[i],false);
			}
		}
		explorer.getNavigator().sort(explorer.getNavigator().getRootNode());
	}
}