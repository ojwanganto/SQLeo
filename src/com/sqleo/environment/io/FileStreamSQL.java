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

package com.sqleo.environment.io;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.sqleo.environment.Preferences;
import com.sqleo.environment.mdi.DialogPreferences;
import com.sqleo.querybuilder.QueryModel;
import com.sqleo.querybuilder.syntax.SQLParser;


public class FileStreamSQL
{
	/* reader */
	public static QueryModel read(String filename)
		throws IOException, ClassNotFoundException
	{
		String sqlRead = readSQL(filename);
		
		return SQLParser.toQueryModel(sqlRead);
	}
	
	public static String readSQL(String filename) throws IOException {
		Reader in = new FileReader(filename);
		StringBuffer sb = new StringBuffer();
		
		int nch;
		char[] buff = new char[4096];
		while ((nch = in.read(buff, 0, buff.length)) != -1)
		{
			sb.append(new String(buff, 0, nch));
		}
		in.close();
		return sb.toString();
	}
	
	/* writer */
	public static void write(String filename, QueryModel model)
		throws IOException
	{
		writeSQL(filename,model.toString(true));
	}
	
	public static void writeSQL(String filename,String sql) throws IOException {
		Writer out = new FileWriter(filename);
		out.write(sql);
		out.flush();
		out.close();
	}
	
}
