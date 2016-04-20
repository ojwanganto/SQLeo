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

import java.io.*;
import java.util.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.comparer.data.DataComparerCriteriaPane;
import com.sqleo.environment.ctrl.comparer.data.DataComparerDialogTable.DATA_TYPE;
import com.sqleo.environment.mdi.ClientContent;
import com.sqleo.environment.mdi.ClientMetadataExplorer;
import com.sqleo.environment.mdi._ConnectionListener;
import com.sqleo.querybuilder.syntax.SQLParser;


public class DataComparer extends BorderLayoutPanel implements _ConnectionListener
{
	private DataComparerCriteriaPane source;
	private DataComparerCriteriaPane target;
	private JCheckBox onlyDifferentValues;
	private JCheckBox addDiffStatusInOutput;

	private static final String DATACOMPARER_WORKINGCONNECTION_URL = "datacomparer.workingconnection.url";
	private JComboBox<String> cbxWorkingConnection;
	private ClientMetadataExplorer cme;

	public void onConnectionClosed(String keycah){
		if (!cbxWorkingConnection.getSelectedItem().toString().equals(keycah)) {
			cbxWorkingConnection.removeItem(keycah);
		}
	}

	public void onConnectionOpened(String keycah){
		addToWorkingConnection(keycah, false);
	}

	public DataComparer()
	{
		super(2,2);

		source = new DataComparerCriteriaPane("SOURCE", this, true);
		target = new DataComparerCriteriaPane("TARGET", this, false);

		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,source,target);
		split.setResizeWeight(.5d);
		setComponentCenter(split);

		final JPanel buttonPanel = new JPanel();

		Application.window.addListener(this);
		final JPanel workPanel = new JPanel();
		final JLabel workingConnLbl = new JLabel(I18n.getString("datacomparer.WorkingConnection","Working connection:"));
		workPanel.add(workingConnLbl);
		cbxWorkingConnection = new JComboBox(ConnectionAssistant.getHandlers().toArray());
		cbxWorkingConnection.setSelectedItem(null);
	//	addToWorkingConnection("", true);

		if(Preferences.containsKey(DATACOMPARER_WORKINGCONNECTION_URL)){
			addToWorkingConnection(Preferences.getString(DATACOMPARER_WORKINGCONNECTION_URL), true);
		}
		workPanel.add(cbxWorkingConnection);
		buttonPanel.add(workPanel);

		onlyDifferentValues = new JCheckBox(I18n.getString("datacomparer.onlyDifferentValues", "Only different values"));
		buttonPanel.add(onlyDifferentValues);
		addDiffStatusInOutput = new JCheckBox(I18n.getString("datacomparer.addDiffStatusInOutput", "Add diff status in output"));
		buttonPanel.add(addDiffStatusInOutput);
		buttonPanel.add(startComparerButton());
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	public void addToWorkingConnection(final String keycah, boolean autoselect){
		boolean found = false;
		for(int i=0; i<cbxWorkingConnection.getItemCount();i++){
			if(keycah.equals(cbxWorkingConnection.getItemAt(i))){
				if(autoselect || null == cbxWorkingConnection.getSelectedItem()){
					cbxWorkingConnection.setSelectedIndex(i);
				}
				found = true;
				break;
			}
		}
		if(!found){
			cbxWorkingConnection.addItem(keycah);
			if(autoselect ){
				cbxWorkingConnection.setSelectedItem(keycah);
			}
		}
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

				String filePath = null;
				try	{

					// merged.csv
					final File file = File.createTempFile("merged_", ".csv");
					filePath = file.getAbsolutePath();
					final PrintStream stream = new PrintStream(new FileOutputStream(file));
					file.deleteOnExit();

					stream.println(getColumnHeaderRow(columns, sourceAggregates, targetAggregates));

					// retrieve source data in parallel
					Thread thread_retrieve1 = new Thread(new Runnable() {
				            @Override
				            public void run() {
						target.retrieveData(stream);
						if(!target.isQueryExecutionSuccess()){
							return;
						}
				            }
				        });
					thread_retrieve1.start();

					source.retrieveData(stream);
					if(!source.isQueryExecutionSuccess()){
						return;
					}

					// wait for first thread to finish
					thread_retrieve1.join();
				} catch (InterruptedException e) {
					// 
				} catch (FileNotFoundException e){
					Application.println(e,true);
				} catch (IOException e) {
					Application.println(e,true);
				}

				// ticket #348 	data comparer: allow million lines comparison
				try {
					sortAndMergeLines(filePath,columns);
				} catch (Exception e){
					Application.println(e,true);
				}
			

				final File mergedCsvFile = new File(filePath);
				final String mergedTableName = mergedCsvFile.getName().substring(0, mergedCsvFile.getName().lastIndexOf("."));;
				// get merged query 
				final String mergedQuery = getMergedQuery(mergedTableName, columns, sourceAggregates, targetAggregates, source.getHeaderAlias(), target.getHeaderAlias());

				final String tempFilePath = filePath.substring(0,filePath.lastIndexOf(File.separator));
				final String tempCsvConnectionUrl= "jdbc:relique:csv:" + tempFilePath + "?separator=;";

				final String workingConnectionJdbcKeyCh;
				if(cbxWorkingConnection.getSelectedItem()!=null){
					workingConnectionJdbcKeyCh=cbxWorkingConnection.getSelectedItem().toString();
					Preferences.set(DATACOMPARER_WORKINGCONNECTION_URL, workingConnectionJdbcKeyCh);
				}else{
					workingConnectionJdbcKeyCh="";
				}

				if("" == workingConnectionJdbcKeyCh){
					final StringBuilder messageBuilder = new StringBuilder();
					messageBuilder.append("Please create and OPEN a csvjdbc connection using jar file\n"); 
					messageBuilder.append("provided in sqleo/lib directory with\n");
					messageBuilder.append("DRIVER: ").append("org.relique.jdbc.csv.CsvDriver").append("\n");
					messageBuilder.append("URL: ").append(tempCsvConnectionUrl);
					messageBuilder.append("\n\nThen RE-LAUNCH the data comparer!");
					Application.alertAsText(messageBuilder.toString());
				}else{
					// open connection to working connection jdbc
					cme = cme!=null ? cme : (ClientMetadataExplorer)Application.window.getClient(ClientMetadataExplorer.DEFAULT_TITLE);
					try {
						cme.getControl().getNavigator().connect(workingConnectionJdbcKeyCh);
					} catch (Exception e) {
						Application.println(e, true);
					}

					// ticket #320 specific for H2 connection
					if (cbxWorkingConnection.getSelectedItem().toString().startsWith("H2")) {
						ConnectionHandler ch = ConnectionAssistant.getHandler(cbxWorkingConnection.getSelectedItem().toString());
						Statement stmt = null;
						try {
							stmt = ch.get().createStatement();
							stmt.executeUpdate("CREATE VIEW "+ mergedTableName + " as SELECT * FROM csvread('" + tempFilePath + "/" + mergedTableName + ".csv',null,'fieldSeparator=;')");
							stmt.close();
						} catch (SQLException sqle) {
							Application.println(sqle, true);
						}
					}

					// open content window on above connection and run merged query
					Application.session.addSQLToHistory(new SQLHistoryData(new Date(), workingConnectionJdbcKeyCh, "DataComparer", mergedQuery));
					try {
						final ClientContent client = new ClientContent(workingConnectionJdbcKeyCh, SQLParser.toQueryModel(mergedQuery),null);
						Application.window.add(client);
					} catch (IOException e) {
						Application.println(e, true);
					}
				}
			}

		};
		final CommandButton compare = new CommandButton(action);
		compare.setText(I18n.getString("datacomparer.start","Start"));
		return compare;
	}


	public int nthOccurrence(String str, String c, int n) {
		if(n <= 0){
	        	return -1;
		}
		int pos = str.indexOf(c, 0);
		while (n-- > 1 && pos != -1)
			pos = str.indexOf(c, pos+1);
		return pos;
	}

    public void sortAndMergeLines(String file, String columns) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        TreeMap<String, String> rows = new TreeMap<String, String>();
        String line="";
	String header = reader.readLine();
	if(!columns.isEmpty()){
		int countCols = columns.length() - columns.replace(",", "").length() + 1;
	        while((line=reader.readLine())!=null){
			String currKey = line.substring(0,nthOccurrence(line,";",countCols));
			String prevLine = rows.get(currKey);
			if (prevLine != null){
				String[] currVal = line.split(";",-1);
				String[] prevVal = prevLine.split(";",-1);
				String   newLine = currKey;
				for (int z = countCols ; z < currVal.length ; z++){
					if (!prevVal[z].equals("\"\"")) currVal[z]=prevVal[z];
	        			newLine = newLine + ";" + currVal[z];
				}
				rows.put(currKey,newLine);      
			} else {
				rows.put(currKey,line);
			}
	        }
	} else { // no column --> 2 rows
	        while((line=reader.readLine())!=null){
			String currKey = "noCols";
			String prevLine = rows.get(currKey);
			if (prevLine != null){
				String[] currVal = line.split(";",-1);
				String[] prevVal = prevLine.split(";",-1);
 				String   newLine = "";
				for (int z = 0 ; z < currVal.length ; z++){
					if (!prevVal[z].equals("\"\"")) currVal[z]=prevVal[z];
	        			newLine = newLine + currVal[z] + ";";
				}
				newLine = newLine.substring(0,newLine.length()-1);
				rows.put(currKey,newLine);      
			} else {
				rows.put(currKey,line);
			}
	        }
	}
        reader.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	writer.write(header);
        writer.newLine();
	for (String row: rows.values()){
		writer.write(row);
        	writer.newLine();
        }
        writer.close();
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
			result.append("SRC").append(i).append(" as \"").append(realSourceAlias).append("_").append(sourceAggregates[i-1]).append("\",");
			result.append("TGT").append(i).append(" as \"").append(realTargetAlias).append("_").append(targetAggregates[i-1]).append("\"");
			// ticket #260 add diff status for each line (can help when exporting in excel AND pitvot table
			if(addDiffStatusInOutput.isSelected()){
				result.append(",\n").append(" case when SRC").append(i).append("=").append("TGT").append(i).append(" then 'EQU'");
				result.append(" when TGT").append(i).append("='' then '").append(realSourceAlias).append("'");	
				result.append(" when SRC").append(i).append("='' then '").append(realTargetAlias).append("'");	
				result.append(" else 'DIFF' end as \"DIFF").append("_").append(sourceAggregates[i-1]).append("\"");	
			}
			if(i<totalAggregates){
				result.append(",\n");
			}
		}

		result.append("\nFROM ").append(mergedTableName).append(" ").append(tableAlias);
		if(onlyDifferentValues.isSelected()){
			result.append("\nWHERE ");
			for(int i = 1; i<=totalAggregates; i++){
				result.append("coalesce(SRC").append(i).append(",'null')!=").append("coalesce(TGT").append(i).append(",'null')");
				if(i<totalAggregates){
					result.append(" OR\n");
				}
			}
		}

// ORDER BY not needed any more as CSV data is sorted (by TreeMap) 
//		if(colsGiven){
//			result.append("\nORDER BY ").append(colsWithAlias.toString());
//		}

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