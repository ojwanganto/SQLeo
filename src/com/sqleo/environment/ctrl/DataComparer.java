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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.comparer.data.DataComparerCriteriaPane;
import com.sqleo.environment.ctrl.comparer.data.DataComparerDialogTable.DATA_TYPE;
import com.sqleo.environment.mdi.ClientContent;
import com.sqleo.querybuilder.syntax.SQLParser;


public class DataComparer extends BorderLayoutPanel
{
	private DataComparerCriteriaPane source;
	private DataComparerCriteriaPane target;
	private JCheckBox onlyDifferentValues;
	private JCheckBox addDiffStatusInOutput;
	
	public DataComparer()
	{
		super(2,2);
		
		source = new DataComparerCriteriaPane("SOURCE", this, true);
		target = new DataComparerCriteriaPane("TARGET", this, false);
		
		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,source,target);
		split.setResizeWeight(.5d);
		setComponentCenter(split);
		
		final JPanel buttonPanel = new JPanel(); 
		onlyDifferentValues = new JCheckBox(I18n.getString("datacomparer.onlyDifferentValues", "Only different values"));
		buttonPanel.add(onlyDifferentValues);
		addDiffStatusInOutput = new JCheckBox(I18n.getString("datacomparer.addDiffStatusInOutput", "Add diff status in output"));
		buttonPanel.add(addDiffStatusInOutput);
		buttonPanel.add(startComparerButton());
		add(buttonPanel, BorderLayout.PAGE_END);
	}
	
	public DataComparerCriteriaPane getSource(){
		return source;
	}
	
	public DataComparerCriteriaPane getTarget(){
		return target;
	}
	
	private JButton startComparerButton() {
		final AbstractAction action = new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				source.setQuery();
				target.setQuery();
				if(!source.validatePanel()){
					return;
				}
				if(!target.validatePanel()){
					return;
				}
				
				final boolean generateMergeCsv = source.getSyntax()!=null && target.getSyntax()!=null;
				if(!generateMergeCsv) return;
				
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

					stream.println(getColumnHeaderRow(columns, sourceAggregates, targetAggregates));
					source.retrieveData(stream);
					if(!source.isQueryExecutionSuccess()){
						return;
					}
					target.retrieveData(stream);
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
			    final String mergedQuery = getMergedQuery(mergedTableName, columns, sourceAggregates, targetAggregates, source.getHeaderAlias(), target.getHeaderAlias());
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
					Application.session.addSQLToHistory(new SQLHistoryData(new Date(), 
							csvjdbcKeych, "DataComparer", mergedQuery));
					try {
						final ClientContent client = new ClientContent(csvjdbcKeych, SQLParser.toQueryModel(mergedQuery),null);
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
		compare.setText(I18n.getString("datacomparer.start","Start"));
		return compare;
	}
	
	private String getColumnHeaderRow(final String columns,
					final String[] sourceAggregates,final String[] targetAggregates){
		final StringBuffer buffer = new StringBuffer();
		if(columns!=null && !columns.isEmpty()){
			for(final String column : columns.split(",")){
				buffer.append(column).append(";");
			}
		}
		for(int i = 1; i<=sourceAggregates.length; i++){
			buffer.append("SRC").append(i).append(";");
			buffer.append("TGT").append(i).append(";");
		}
//		for(final String sourceAggr : sourceAggregates){
//			final String sourceAggrName = getMatchingAggregateName(sourceAggr, targetAggregates);
//			if(sourceAggrName!=null){
//				buffer.append(sourceAggrName).append(";");
//			}
//		}
		if(buffer.length() > 0) buffer.deleteCharAt(buffer.length()-1);
		return buffer.toString();
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
			final String[] sourceAggregates,final String[] targetAggregates,final String sourceAlias,final String targetAlias){
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
		final String realSourceAlias = sourceAlias!=null?sourceAlias:"SOURCE";
		final String realTargetAlias = targetAlias!=null?targetAlias:"TARGET";
		for(int i = 1; i<=totalAggregates; i++){
			result.append("MAX(SRC").append(i).append(") as \"agg").append(i).append("_").append(realSourceAlias).append("\",");
			result.append("MAX(TGT").append(i).append(") as \"agg").append(i).append("_").append(realTargetAlias).append("\"");
			// ticket #260 add diff status for each line (can help when exporting in excel AND pitvot table
			if(addDiffStatusInOutput.isSelected()){
				result.append(",\n").append(" case when MAX(SRC").append(i).append(")=").append("MAX(TGT").append(i).append(") then 'EQU'");
				result.append(" when MAX(TGT").append(i).append(")='' then '").append(realSourceAlias).append("'");	
				result.append(" when MAX(SRC").append(i).append(")='' then '").append(realTargetAlias).append("'");	
				result.append(" else 'DIFF' end as DIFF").append(i);	
			}
			if(i<totalAggregates){
				result.append(",\n");
			}
		}

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
		addDiffStatusInOutput.setSelected(setup.isAddDiffStatusInOutput());
	}

	public DataComparerConfig getSetup() {
		final DataComparerConfig setup = new DataComparerConfig();
		setup.setSourcePanelConfig(source.getSetup());
		setup.setTargetPanelConfig(target.getSetup());
		setup.setOnlyDifferentValues(onlyDifferentValues.isSelected());
		setup.setAddDiffStatusInOutput(addDiffStatusInOutput.isSelected());
		return setup;
	}
	
}