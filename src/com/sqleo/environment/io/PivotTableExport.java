package com.sqleo.environment.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.sql.SQLException;

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
			stream.println("document.write('<thead><tr>')");
			//print columns
			stream.print("document.write('");
			stream.print(getColumnHeaderRow());
			stream.println("')");
			stream.println("document.write('</tr></thead>')");
			stream.println("document.write('<tbody>')");

			//print rows
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

	protected abstract String getColumnHeaderRow() throws SQLException;

	protected abstract void printRows(final PrintStream stream) throws SQLException;
	
	protected void appendTableHeader(final StringBuffer buffer, final String text){
		buffer.append("<th>").append(text).append("</th>");
	}
	
	protected void appendTableData(final StringBuffer buffer, final String text){
		buffer.append("<td>").append(text).append("</td>");
	}
	
	protected void writeTableRow(final PrintStream stream,final String row){
		stream.print("document.write('<tr>");
		stream.print(row);
		stream.println("</tr>')");
	}
}
