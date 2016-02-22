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

package com.sqleo.environment.ctrl.comparer.data;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.font.TextAttribute;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.DataComparerPanelConfig;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.common.util.SQLHistoryData;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.DataComparer;
import com.sqleo.environment.ctrl.comparer.data.DataComparerDialogTable.DATA_TYPE;
import com.sqleo.environment.ctrl.editor.Task;
import com.sqleo.environment.ctrl.editor._TaskSource;
import com.sqleo.environment.ctrl.editor._TaskTarget;
import com.sqleo.environment.mdi.MDIClient;
import com.sqleo.environment.mdi._ConnectionListener;


public class DataComparerCriteriaPane extends JPanel implements _ConnectionListener, ItemListener, _TaskSource{
	
	private static final int LIMITED_ROWS_FOR_FREE_VERSION = 100;
	private JComboBox cbxConnection;
	private JComboBox cbxSchema;
	private JTextField txtTable;
	private Map<DATA_TYPE,DataComparerCriteriaDialogPane> dataTypePanelMap = 
		new HashMap<DATA_TYPE,DataComparerCriteriaDialogPane>(3);
	private String query;
	private DataComparer owner;
	private boolean queryExecutionSuccess = false;
	private boolean isSource;
	private JTextField headerAlias;

	public DataComparerCriteriaPane(final String headerText,final DataComparer owner,final boolean isSource){
		this.owner = owner;
		this.isSource = isSource;
		Application.window.addListener(this);
		
		setBackground(Color.white);
		setBorder(LineBorder.createGrayLineBorder());
		
		initComponents(headerText);
	}
	
	public void removeListener(){
		Application.window.removeListener(this);
	}
	
	private void initComponents(final String headerText){
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		gbc.anchor = GridBagConstraints.NORTH;

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		headerAlias = new JTextField();
		headerAlias.setText(headerText);
		headerAlias.setColumns(20);

		final Font font = new Font(headerAlias.getFont().getName(), Font.BOLD, headerAlias.getFont().getSize());
		final Map<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
		map.put(TextAttribute.FONT, font);
		map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		headerAlias.setFont(Font.getFont(map));
		
		gbl.setConstraints(headerAlias, gbc);
		add(headerAlias);

		gbc.insets = new Insets(5,5,5,5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;

		gbc.gridx = 0;
		gbc.gridy = 1;
		final JLabel useConnections = new JLabel(I18n.getString("application.message.useConnection","Use connection:"));
		gbl.setConstraints(useConnections, gbc);
		add(useConnections);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		cbxConnection	= new JComboBox(ConnectionAssistant.getHandlers().toArray());
		gbl.setConstraints(cbxConnection, gbc);
		add(cbxConnection);
		cbxConnection.setSelectedItem(null);
		cbxConnection.addItemListener(this);

		gbc.gridx = 0;
		gbc.gridy = 2;
		final JLabel schemas = new JLabel(I18n.getString("application.message.schema","Schema"));
		gbl.setConstraints(schemas, gbc);
		add(schemas);

		gbc.gridx = 1;
		gbc.gridy = 2;
		cbxSchema = new JComboBox();
		gbl.setConstraints(cbxSchema, gbc);
		add(cbxSchema);
		cbxSchema.setEnabled(false);

		gbc.gridx = 0;
		gbc.gridy = 3;
		final JLabel table = new JLabel(I18n.getString("datacomparer.TableViewQuery","Table/View or (Query):"));
		gbl.setConstraints(table, gbc);
		add(table);

		gbc.gridx = 1;
		gbc.gridy = 3;
		txtTable = new JTextField();
		gbl.setConstraints(txtTable, gbc);
		add(txtTable);
		
		gbc.gridwidth = 2;
		gbc.gridheight = 2;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
        
		gbc.gridx = 0;
		gbc.gridy = 4;
		addDataComparerDialogPane(gbc, gbl, DATA_TYPE.COLUMNS);

		gbc.gridx = 0;
		gbc.gridy = 6;
		addDataComparerDialogPane(gbc, gbl, DATA_TYPE.AGGREGATES);

		gbc.gridx = 0;
		gbc.gridy = 8;
		addDataComparerDialogPane(gbc, gbl, DATA_TYPE.FILTERS);
		
		gbc.gridx = 0;
		gbc.gridy = 10;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.CENTER;
		final JButton query = new JButton();
		query.setAction(new ShowQueryAction(new QueryDialog()));

		if(isSource){
			gbl.setConstraints(query,gbc);
			add(query);
		}else{
			gbc.weightx = 0;
			gbc.weighty = 0.2;
			gbl.setConstraints(query,gbc);
			add(query);
			
			gbc.gridx = 0;
			gbc.gridy = 12;
			final JTextArea label=  new JTextArea(I18n.getString("datacomparer.RightSideText","Note: Right side texts are automatically\ncopied from left when empty!"));
			label.setEditable(false);
			label.setBackground(Color.yellow);
			gbl.setConstraints(label,gbc);
			add(label);
			
		}
	}
	
	private class QueryDialog extends AbstractDialogConfirm{
		private JTextArea syntax;
		
		protected QueryDialog() {
			super(Application.window, I18n.getString("datacomparer.generatedQueryButton", "Generated query text"));
			
			syntax = new JTextArea();
			syntax.setWrapStyleWord(true);
			syntax.setEditable(false);
			syntax.setLineWrap(true);
			syntax.setOpaque(true);
			syntax.setTabSize(4);
			syntax.setCaretPosition(0);
			syntax.setBackground(Color.white);

			getContentPane().add(new JScrollPane(syntax));
		}

		@Override
		protected boolean onConfirm() {
			query = syntax.getText();
			return true;
		}

		@Override
		protected void onOpen() {
			syntax.setText(query);
		}
		
	}
	
	private void updateTargetTextIfEmpty() {
		final Thread t  = new Thread(new Runnable() {
			@Override
			public void run() {
				final String table = owner.getTarget().getTable();
				if(null == table || table.isEmpty()){
					owner.getTarget().txtTable.setText(owner.getSource().getTable());
				}
				setTextIfEmpty(DATA_TYPE.COLUMNS);
				setTextIfEmpty(DATA_TYPE.AGGREGATES);
				setTextIfEmpty(DATA_TYPE.FILTERS);
			}
			private void setTextIfEmpty(final DATA_TYPE dataType){
				final String text = owner.getTarget().getDataType(dataType);
				if(null == text || text.isEmpty()) {
					owner.getTarget().getDataTypePanelMap().get(dataType).setText(getDataType(dataType));
				}
				final String textS = owner.getTarget().getDataType(dataType);
				if(null == textS || textS.isEmpty()) {
					owner.getTarget().getDataTypePanelMap().get(dataType).setText(owner.getSource().getDataType(dataType));
				}
			}
		});
		t.start();
		try {
			Thread.currentThread().sleep(100L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public Map<DATA_TYPE, DataComparerCriteriaDialogPane> getDataTypePanelMap () {
		return dataTypePanelMap;
	}
	
	public boolean validatePanel(){
		final String target = isSource ? "SOURCE" : "TARGET";
		if(cbxConnection.getSelectedIndex() < 0){
			Application.alert(I18n.getString("datacomparer.ConnexionNotSet","No connection selected in ")+target);
			return false;
		}
		if(txtTable.getText().isEmpty()){
			Application.alert(I18n.getString("datacomparer.TableNotSet","No table/query entered in ")+target);
			return false;
		}
		return true;
	}
	
	public void setQuery(){
		updateTargetTextIfEmpty();

		final String cols =  dataTypePanelMap.get(DATA_TYPE.COLUMNS).getText();
		final String filters =  dataTypePanelMap.get(DATA_TYPE.FILTERS).getText();
		final String sourceAggrText = owner.getSource().getDataType(DATA_TYPE.AGGREGATES);
		final String targetAggrText = owner.getTarget().getDataType(DATA_TYPE.AGGREGATES);
		final String[] sourceAggregates = sourceAggrText!=null ? sourceAggrText.split(",") : new String[0];
		final String[] targetAggregates = targetAggrText!=null ? targetAggrText.split(",") : new String[0];

		final StringBuilder val = new StringBuilder();
		boolean selectAppended = false;
		final boolean columnsGiven = cols!=null && !cols.isEmpty();
		if(columnsGiven){
			val.append("SELECT ");
			selectAppended = true;
			val.append("\n"+cols);
		}
		final boolean aggregatesGiven;
		if(this == owner.getSource()){
			aggregatesGiven = sourceAggrText!=null && !sourceAggrText.isEmpty();
		}else{
			aggregatesGiven = targetAggrText!=null && !targetAggrText.isEmpty();
		}
		if(aggregatesGiven){
			if(!selectAppended){
				val.append("SELECT ");
				selectAppended = true;
			}else{
				val.append(",");
			}
			val.append("\n");
			if(this == owner.getSource()){
				int i = 1;
				for(String aggrSplitted : sourceAggregates){
					val.append(aggrSplitted).append(" AS SRC").append(i).append(",");
					val.append("'\"\"'").append(" AS TGT").append(i).append(",");
					i++;
				}
			}else{
				int i = 1;
				for(String aggrSplitted : targetAggregates){
					val.append("'\"\"'").append(" AS SRC").append(i).append(",");
					val.append(aggrSplitted).append(" AS TGT").append(i).append(",");
					i++;
				}
			}
			val.deleteCharAt(val.length()-1);
		}
		if(selectAppended){
			final String tableFinalName =
				getSchema()!=null ? getSchema()+"."+getTable() : getTable();
			val.append("\nFROM ").append(tableFinalName);
			if(filters!=null && !filters.isEmpty()){
				val.append("\nWHERE ").append(filters);
			}
			if(columnsGiven && aggregatesGiven){
				val.append("\nGROUP BY ").append(cols);
			}
		}
		query = val.toString();
	}
	
	private class ShowQueryAction extends AbstractAction{
		private final QueryDialog dialog;
		public ShowQueryAction(final QueryDialog dialog){
			super(dialog.getTitle());
			this.dialog = dialog;
		}
        
		public void actionPerformed(ActionEvent ae){
			setQuery();
			dialog.onOpen();
			dialog.setVisible(true);
		}
	}

	private void addDataComparerDialogPane(GridBagConstraints gbc,
			GridBagLayout gbl,DATA_TYPE dataType) {
		final DataComparerCriteriaDialogPane panel = 
			new DataComparerCriteriaDialogPane(dataType, this);
		dataTypePanelMap.put(dataType,panel);
		gbl.setConstraints(panel,gbc);
		add(panel);
	}
	
	public void onConnectionClosed(String keycah){
		cbxConnection.removeItem(keycah);
	}

	public void onConnectionOpened(String keycah){
		boolean found = false;
		for(int i=0; i<cbxConnection.getItemCount();i++){
			if(keycah.equals(cbxConnection.getItemAt(i))){
				found = true;
				break;
			}
		}
		if(!found){
			cbxConnection.addItem(keycah);
		}
	}

	public void setEnabled(boolean b){
		cbxConnection.setEnabled(b);
		cbxSchema.setEnabled(b && cbxSchema.getItemCount()>0);
		txtTable.setEnabled(b);
	}

	public void onFinished(){
		setEnabled(true);
	}
	
	public void itemStateChanged(ItemEvent ie){
		final String keycah = ie.getItem().toString();
		ConnectionHandler ch = ConnectionAssistant.getHandler(keycah);
		setBackground(MDIClient.getConnectionBackgroundColor(keycah));
		
		if(ch == null)
		{
			cbxSchema.setModel(new DefaultComboBoxModel());
		}
		else
		{
			ArrayList schemas = (ArrayList)ch.getObject("$schema_names");
			final Object[] array = new Object[schemas.size()+1];
			array[0] = "";
			int i = 1; 
			for(Object schema : schemas){
				array[i] = schema;
				i++;
			}
			cbxSchema.setModel(new DefaultComboBoxModel(array));
		}
		
		cbxSchema.setEnabled(cbxSchema.getItemCount()>0);
	}
	
	public String getSchema(){
		return cbxSchema.getSelectedIndex() != -1 ? 
				(!cbxSchema.getSelectedItem().toString().isEmpty() ? cbxSchema.getSelectedItem().toString() :  null)  : null;
	}

	public String getTable(){
		return txtTable.getText();
	}
	
	public String getHeaderAlias() {
		return headerAlias.getText();
	}
	
	public String[] getTableColumns(){
		return SQLHelper.getColumns(getHandlerKey(), getSchema(), getTable());
	}
	
	public String getDataType(final DATA_TYPE dataType){
		return dataTypePanelMap.get(dataType).getText();
	}

	@Override
	public String getHandlerKey() {
		return cbxConnection.getSelectedIndex() != -1 ? cbxConnection.getSelectedItem().toString() : null;
	}

	@Override
	public String getSyntax() {
		return query;
	}
	
	public void retrieveData(final PrintStream stream){
		queryExecutionSuccess = false;
		final boolean isFullVersion = Application.isFullVersion();
		final _TaskTarget target = new _TaskTarget(){
			@Override
			public boolean continueRun() {
				return false;
			}
			@Override
			public void onTaskFinished(String message, boolean error) {
				if(error){
					Application.alert(message);
					queryExecutionSuccess = false;
				}else{
					queryExecutionSuccess = true;
				}
			}
			@Override
			public void write(String text) {
			}
			@Override
			public boolean printSelect() {
				return false;
			}
			@Override
			public void processResult(ResultSet rs) {
				try {
					int cols= rs.getMetaData().getColumnCount();
					Object[] vals = null;
					int rowCount = 0;
					// fetch size optimisation (mainly for Oracle that as a default fetch size of 10)
					rs.setFetchSize(1000);
					while(rs.next()){
						vals = new Object[cols];
						for(int i=1; i<=cols;i++){
							vals[i-1] = SQLHelper.getRowValue(rs, i);
						}
						final StringBuffer buffer = new StringBuffer();
						for(int i=0; i<vals.length; i++){
							if(vals[i]==null) {
								vals[i]="";
							} else {
								vals[i] = vals[i].toString();
							}
							buffer.append(vals[i] + ";");
						}
						if(buffer.length() > 0) buffer.deleteCharAt(buffer.length()-1);
						stream.println(buffer.toString());
						
						if(!isFullVersion){
							rowCount++;
							if(rowCount > LIMITED_ROWS_FOR_FREE_VERSION) {
								Application.alert(I18n.getString("datacomparer.VersionWith100rowsMax","Version with max 100 rows for data comparer query result, Please Donate for more"));
								break;
							}
						}
					}
				} catch (SQLException e) {
					queryExecutionSuccess = false;
					Application.println(e, true);
				}
			}
			
		};
		Application.session.addSQLToHistory(new SQLHistoryData(new Date(), 
				getHandlerKey(), "DataComparer", getSyntax()));

		new Task(this , target , isFullVersion ? 0 : LIMITED_ROWS_FOR_FREE_VERSION+1).run();
	}
	
	public boolean isQueryExecutionSuccess(){
		return queryExecutionSuccess;
	}
	
	public DataComparerPanelConfig getSetup(){
		final DataComparerPanelConfig panelConfig = new DataComparerPanelConfig();
		panelConfig.setConnection(getHandlerKey());
		panelConfig.setSchema(getSchema());
		panelConfig.setTableOrQuery(getTable());
		panelConfig.setHeaderAlias(getHeaderAlias());
		panelConfig.setColumns(dataTypePanelMap.get(DATA_TYPE.COLUMNS).getText());
		panelConfig.setAggregates(dataTypePanelMap.get(DATA_TYPE.AGGREGATES).getText());
		panelConfig.setFilters(dataTypePanelMap.get(DATA_TYPE.FILTERS).getText());
		return panelConfig;
	}

	public void loadSetup(final DataComparerPanelConfig panelConfig) {
		cbxConnection.setSelectedItem(panelConfig.getConnection());
		cbxSchema.setSelectedItem(panelConfig.getSchema());
		txtTable.setText(panelConfig.getTableOrQuery());
		if(panelConfig.getHeaderAlias()!=null){
			headerAlias.setText(panelConfig.getHeaderAlias());
		}
		dataTypePanelMap.get(DATA_TYPE.COLUMNS).setText(panelConfig.getColumns());
		dataTypePanelMap.get(DATA_TYPE.AGGREGATES).setText(panelConfig.getAggregates());
		dataTypePanelMap.get(DATA_TYPE.FILTERS).setText(panelConfig.getFilters());
	}

}
