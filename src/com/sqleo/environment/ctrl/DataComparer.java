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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.CommandButton;
import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.comparer.CriteriaPane;
import com.sqleo.environment.ctrl.comparer.ReportPane;
import com.sqleo.environment.ctrl.comparer.data.DataComparerCriteriaPane;
import com.sqleo.environment.ctrl.comparer.data.DataComparerDialogTable.DATA_TYPE;
import com.sqleo.environment.ctrl.content.MaskExport;
import com.sqleo.environment.ctrl.editor.Task;
import com.sqleo.environment.ctrl.editor._TaskTarget;
import com.sqleo.environment.io.FileHelper;
import com.sqleo.querybuilder.QueryModel;
import com.sqleo.querybuilder.syntax.QueryTokens._Expression;
import com.sqleo.querybuilder.syntax.SQLParser;


public class DataComparer extends BorderLayoutPanel
{
	private DataComparerCriteriaPane source;
	private DataComparerCriteriaPane target;
	
	public DataComparer()
	{
		super(2,2);
		
		source = new DataComparerCriteriaPane(I18n.getString("datacomparer.source", "SOURCE"));
		target = new DataComparerCriteriaPane(I18n.getString("datacomparer.target", "TARGET"));
		
		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,source,target);
		split.setResizeWeight(.5d);
		setComponentCenter(split);
		
		final JPanel buttonPanel = new JPanel(); 
		buttonPanel.add(getCompareButton());
		add(buttonPanel, BorderLayout.PAGE_END);
	}
	
	private JButton getCompareButton() {
		final AbstractAction action = new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
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

					stream.println(getColumnHeaderRow(columns, sourceAggregates, targetAggregates));
					source.retrieveData(stream);
					target.retrieveData(stream);
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
			    FileHelper.openFile(mergedCsvFile);
			    final String mergedTableName = mergedCsvFile.getName().substring(0, mergedCsvFile.getName().lastIndexOf("."));;
			    // get merged query 
			    Application.alertAsText(getMergedQuery(mergedTableName, columns, sourceAggregates, targetAggregates));
				
			    //TODO
				// open connection to csvjdbc using merged.csv
				// open command editor / content window on above connection and merged query
				// run the merged query 
			}
		};
		final CommandButton compare = new CommandButton(action);
		compare.setText(I18n.getString("datacomparer.start","Start"));
		return compare;
	}
	
	
	private String getColumnHeaderRow(final String columns,
					final String[] sourceAggregates,final String[] targetAggregates){
		final StringBuffer buffer = new StringBuffer();
		for(final String column : columns.split(",")){
			buffer.append(column).append(";");
		}
		for(int i = 1; i<=sourceAggregates.length; i++){
			buffer.append("MAX(NB").append(i).append(");");
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
			final String[] sourceAggregates,final String[] targetAggregates){
		final StringBuilder result = new StringBuilder();
		result.append("SELECT \n").append(columns).append(",\n");
		for(int i = 1; i<=sourceAggregates.length; i++){
			result.append("MAX(NB").append(i).append("),");
		}
		result.deleteCharAt(result.length()-1);
//		for(final String sourceAggr : sourceAggregates){
//			final String sourceAggrName = getMatchingAggregateName(sourceAggr, targetAggregates);
//			if(sourceAggrName!=null){
//				result.append("\n,MAX(").append(sourceAggrName).append(")");
//			}
//		}
		result.append("\nFROM ").append(mergedTableName);
		result.append("\nGROUP BY ").append(columns);
		result.append("\nORDER BY ").append(columns);
		return result.toString();
	}
	
}