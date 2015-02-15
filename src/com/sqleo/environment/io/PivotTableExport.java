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
package com.sqleo.environment.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.sql.SQLException;

import com.sqleo.common.util.Text;
import com.sqleo.common.util.UriHelper;
import com.sqleo.environment.Application;

public abstract class PivotTableExport {

	public PivotTableExport() throws SQLException{
		PrintStream stream = null;
		try {
			final File jarFile =
				new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			final String absolutePath = jarFile.getAbsolutePath();
			final String jarFileDirectory = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
			final String pivotFile=jarFileDirectory+File.separator+"lib"+File.separator+"pivotInputTable.js";
			stream = new PrintStream(new FileOutputStream(new File(pivotFile)));

			stream.println("document.write('<table id=\"input\" border=\"1\">')");

			//print columns
			printHeaderRow(stream);
			
			//print rows
			stream.println("document.write('<tbody>')");
			printRows(stream);
			stream.println("document.write('</tbody></table>')");
			stream.close();
			
			//open pivot html
			UriHelper.openUrl(new File(jarFileDirectory+File.separator+"lib"+File.separator+"PivotDemo.html"));

		}catch (FileNotFoundException e){
			Application.println(e,true);
		} catch (URISyntaxException e) {
			Application.println(e,true);
		} finally{
			if(stream!=null){
				stream.close();
			}
		}
	}

	protected abstract void printHeaderRow(final PrintStream stream) throws SQLException;

	protected abstract void printRows(final PrintStream stream) throws SQLException;
	
	protected void writeTableHeaderRow(final PrintStream stream,final Object[] vals){
		stream.print("document.write('<thead><tr>");
		final StringBuffer buffer = new StringBuffer();
		for(int i = 0 ; i < vals.length ; i++){
			buffer.append("<th>").append(Text.escapeHTML(vals[i].toString())).append("</th>");
		}
		stream.print(buffer.toString());
		stream.println("</tr></thead>')");
	}
	
	protected void writeTableRow(final PrintStream stream,final Object[] vals){
		stream.print("document.write('<tr>");
		final StringBuffer buffer = new StringBuffer();
		for(int i = 0 ; i < vals.length ; i++){
			buffer.append("<td>").append(Text.escapeHTML(vals[i].toString())).append("</td>");
		}
		stream.print(buffer.toString());
		stream.println("</tr>')");
	}
	

}
