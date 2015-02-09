/*
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
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

package com.sqleo.environment.ctrl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.CommandButton;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.DataComparerConfig;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.SQLHistoryData;
import com.sqleo.common.util.UriHelper;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.comparer.data.DataComparerCriteriaPane;
import com.sqleo.environment.ctrl.comparer.data.DataComparerDialogTable.DATA_TYPE;
import com.sqleo.environment.mdi.ClientContent;
import com.sqleo.environment.mdi.MDIActions;
import com.sqleo.querybuilder.syntax.SQLParser;


public class DataComparer extends BorderLayoutPanel
{
	public static final String CSV_SEP = ";";
	private DataComparerCriteriaPane source;
	private DataComparerCriteriaPane target;
	private JCheckBox onlyDifferentValues;
	
	public DataComparer()
	{
		super(2,2);
		
		source = new DataComparerCriteriaPane(I18n.getString("datacomparer.source", "SOURCE"), this, true);
		target = new DataComparerCriteriaPane(I18n.getString("datacomparer.target", "TARGET"), this, false);
		
		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,source,target);
		split.setResizeWeight(.5d);
		setComponentCenter(split);
		
		final JPanel buttonPanel = new JPanel(); 
		onlyDifferentValues = new JCheckBox(I18n.getString("datacomparer.onlyDifferentValues", "Only different values"));
		buttonPanel.add(onlyDifferentValues);
		buttonPanel.add(getCompareButton());
		final CommandButton startHtmlButton = new CommandButton(new ActionGeneratePivotData()); 
		buttonPanel.add(startHtmlButton);
		onlyDifferentValues.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
            	startHtmlButton.setEnabled(!onlyDifferentValues.isSelected());
            }
        });
		add(buttonPanel, BorderLayout.PAGE_END);
	}
	
	public DataComparerCriteriaPane getSource(){
		return source;
	}
	
	public DataComparerCriteriaPane getTarget(){
		return target;
	}
	
	private class ActionGeneratePivotData extends MDIActions.AbstractBase {
		private ActionGeneratePivotData() {
			setText(I18n.getString("datacomparer.startHtml","Start (HTML)"));
		}
		@Override
		public void actionPerformed(ActionEvent ae) {
			generatePivotData();
		}
	}
	
	
	private JButton getCompareButton() {
		final AbstractAction action = new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				source.setQuery();
				target.setQuery();
				if(null == source.getSyntax() || null == target.getSyntax() ) {
					return;
				}
				
				final String columns = source.getDataType(DATA_TYPE.COLUMNS);
				final String sourceAggregateText = source.getDataType(DATA_TYPE.AGGREGATES);
				final String targetAggregateText = target.getDataType(DATA_TYPE.AGGREGATES);
				final String[] sourceAggregates = sourceAggregateText!=null ? sourceAggregateText.split(",") : new String[0];
				final String[] targetAggregates = targetAggregateText!=null ? targetAggregateText.split(",") : new String[0];

				PrintStream stream = null;
				String filePath = null;
				try	{
					// merged.csv
					final File file = File.createTempFile("merged_", ".csv");
					filePath = file.getAbsolutePath();
					stream = new PrintStream(new FileOutputStream(file));
					file.deleteOnExit();

					stream.println(getColumnHeaderRow(columns, sourceAggregates, targetAggregates, false));
					source.retrieveData(stream, false);
					if(!source.isQueryExecutionSuccess()){
						return;
					}
					target.retrieveData(stream, false);
					if(!target.isQueryExecutionSuccess()){
						return;
					}
				}catch (FileNotFoundException e){
					Application.println(e,true);
				} catch (IOException e) {
					Application.println(e,true);
				}finally{
					if(stream!=null){
						stream.close();
					}
				}
				final File mergedCsvFile = new File(filePath);
			    final String mergedTableName = mergedCsvFile.getName().substring(0, mergedCsvFile.getName().lastIndexOf("."));;
			    // get merged query 
			    final String mergedQuery = getMergedQuery(mergedTableName, columns, sourceAggregates, targetAggregates);
				final String csvjdbcKeych = getCsvJdbcConnectionKey();
				if(null == csvjdbcKeych){
					final StringBuilder messageBuilder = new StringBuilder();
					messageBuilder.append("Please create and OPEN a csvjdbc connection using jar file\n"); 
					messageBuilder.append("provided in sqleo/lib directory with\n");
					messageBuilder.append("DRIVER: ").append("org.relique.jdbc.csv.CsvDriver").append("\n");
					final String tempFilePath = filePath.substring(0,filePath.lastIndexOf(File.separator));
					messageBuilder.append("URL: ").append("jdbc:relique:csv:").append(tempFilePath).append("?separator=;");
					messageBuilder.append("\n\nThen RE-LAUNCH the data comparer!");
					Application.alertAsText(messageBuilder.toString());
				}else{
					// open connection to csvjdbc using merged.csv
					// open content window on above connection and run merged query
					try {
						Application.session.addSQLToHistory(new SQLHistoryData(new Date(), 
								csvjdbcKeych, "DataComparer", mergedQuery));
						final ClientContent client = 
							new ClientContent(csvjdbcKeych, SQLParser.toQueryModel(mergedQuery),null);
						Application.window.add(client);
					} catch (IOException e) {
						Application.println(e, true);
					}
				}
			}
			
			private String getCsvJdbcConnectionKey(){
				for(final Object keych : ConnectionAssistant.getHandlers()){
					final ConnectionHandler ch = ConnectionAssistant.getHandler((String)keych);
					try {
						if(ch.get().getMetaData().getDriverName().equals("CsvJdbc")){
							return (String)keych;
						}
					} catch (SQLException e) {
							e.printStackTrace();
					}
				}
				return null;
			}
		};
		final CommandButton compare = new CommandButton(action);
		compare.setText(I18n.getString("datacomparer.start","Start (Grid)"));
		return compare;
	}
	
	private void generatePivotData() {
		source.setQuery();
		target.setQuery();
		if(null == source.getSyntax() || null == target.getSyntax() ) {
			return;
		}
		
		final String columns = source.getDataType(DATA_TYPE.COLUMNS);
		final String sourceAggregateText = source.getDataType(DATA_TYPE.AGGREGATES);
		final String targetAggregateText = target.getDataType(DATA_TYPE.AGGREGATES);
		final String[] sourceAggregates = sourceAggregateText!=null ? sourceAggregateText.split(",") : new String[0];
		final String[] targetAggregates = targetAggregateText!=null ? targetAggregateText.split(",") : new String[0];

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
			//header columns
			stream.print("document.write('");
			stream.print(getColumnHeaderRow(columns, sourceAggregates, targetAggregates, true));
			stream.println("')");

			stream.println("document.write('</tr></thead>')");
			stream.println("document.write('<tbody>')");
			//rows
			source.retrieveData(stream, true);
			if(!source.isQueryExecutionSuccess()){
				return;
			}
			target.retrieveData(stream,true);
			if(!target.isQueryExecutionSuccess()){
				return;
			}
			stream.println("document.write('</tbody></table>')");
			stream.close();
			
			
			UriHelper.openUrl(new File(jarFileDirectory+File.separator+"lib"+File.separator+"PivotDemo.html"));
		}
		catch (FileNotFoundException e){
			Application.println(e,true);
		} catch (URISyntaxException e) {
			Application.println(e,true);
		} finally{
			if(stream!=null){
				stream.close();
			}
		}
		
		
	}
	
	private String getColumnHeaderRow(final String columns,
					final String[] sourceAggregates,final String[] targetAggregates, final boolean isHtml){
		final StringBuffer buffer = new StringBuffer();
		write(buffer,isHtml,"ENV");
		if(columns!=null && !columns.isEmpty()){
			for(final String column : columns.split(",")){
				write(buffer,isHtml,column);
			}
		}
		for(int i = 1; i<=sourceAggregates.length; i++){
			write(buffer,isHtml,"AGG"+i);
//			write(buffer,isHtml,"TGT"+i);
		}
//		for(final String sourceAggr : sourceAggregates){
//			final String sourceAggrName = getMatchingAggregateName(sourceAggr, targetAggregates);
//			if(sourceAggrName!=null){
//				buffer.append(sourceAggrName).append(";");
//			}
//		}
		if(!isHtml && buffer.length() > 0) buffer.deleteCharAt(buffer.length()-1);
		return buffer.toString();
	}
	
	private void write(final StringBuffer buffer, final boolean isHtml, final String text){
		if(isHtml){
			buffer.append("<th>");
		}
		buffer.append(text);
		if(isHtml){
			buffer.append("</th>");
		}else{
			buffer.append(CSV_SEP);
		}
	}
	
	private String getRealAggregateName(final String aggregate){
		final StringTokenizer tokenizer = new StringTokenizer(aggregate);
		if(tokenizer.hasMoreTokens()){
			final String function = tokenizer.nextToken();
			if(tokenizer.hasMoreTokens()){
				final String alias = tokenizer.nextToken();
				if(alias.toUpperCase().equals("AS")){
					if(tokenizer.hasMoreTokens()){
						return tokenizer.nextToken();
					}
				}else{
					return alias;
				}
			}else{
				return function;
			}
		}
		return "";
	}

	private String getMatchingAggregateName(final String sourceAggr, final String[] targetAggregates){
		final String sourceAggrName = getRealAggregateName(sourceAggr);
		for(final String targetAggr : targetAggregates){
			final String targetAggrName = getRealAggregateName(targetAggr);
			if(sourceAggrName.equals(targetAggrName)){
				return sourceAggrName;
			}
		}
		return null;
	}
	
	private String getMergedQuery(final String mergedTableName,final String columns,
			final String[] sourceAggregates,final String[] targetAggregates){
		final String tableAlias = "data";
		final boolean colsGiven = columns!=null && !columns.isEmpty();
		final StringBuilder colsWithAlias = new StringBuilder();
		if(colsGiven){
			for(String col : columns.split(",")){
				colsWithAlias.append(tableAlias).append(".").append(col).append(",");
			}
			colsWithAlias.deleteCharAt(colsWithAlias.length()-1);
		}
		final StringBuilder result = new StringBuilder();
		result.append("SELECT \n");
		if(colsGiven){
			result.append(colsWithAlias.toString()).append(",\n");
		}
		final int totalAggregates = sourceAggregates.length;
		for(int i = 1; i<=totalAggregates; i++){
			result.append("MAX(SRC").append(i).append(") as agg_SOURCE").append(i).append(",");
			result.append("MAX(TGT").append(i).append(") as agg_TARGET").append(i);
			if(i<totalAggregates){
				result.append(",\n");
			}
		}
//		for(final String sourceAggr : sourceAggregates){
//			final String sourceAggrName = getMatchingAggregateName(sourceAggr, targetAggregates);
//			if(sourceAggrName!=null){
//				result.append("\n,MAX(").append(sourceAggrName).append(")");
//			}
//		}
		result.append("\nFROM ").append(mergedTableName).append(" ").append(tableAlias);
		if(colsGiven){
			result.append("\nGROUP BY ").append(colsWithAlias.toString());
		}
		if(onlyDifferentValues.isSelected()){
			result.append("\nHAVING ");
			for(int i = 1; i<=totalAggregates; i++){
				result.append("MAX(SRC").append(i).append(")!=").append("MAX(TGT").append(i).append(")");
				if(i<totalAggregates){
					result.append(" OR\n");
				}
			}
		}
		if(colsGiven){
			result.append("\nORDER BY ").append(colsWithAlias.toString());
		}
		
		return result.toString();
	}

	public void loadSetup(final DataComparerConfig setup) {
		if(null == setup)
			return;
		source.loadSetup(setup.getSourcePanelConfig());
		target.loadSetup(setup.getTargetPanelConfig());
		onlyDifferentValues.setSelected(setup.isOnlyDifferentValues());
	}

	public DataComparerConfig getSetup() {
		final DataComparerConfig setup = new DataComparerConfig();
		setup.setSourcePanelConfig(source.getSetup());
		setup.setTargetPanelConfig(target.getSetup());
		setup.setOnlyDifferentValues(onlyDifferentValues.isSelected());
		return setup;
	}

	
	
}