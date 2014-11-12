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
import java.sql.Types;
import java.util.ArrayList;
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
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.common.util.Text;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.comparer.data.DataComparerDialogTable.DATA_TYPE;
import com.sqleo.environment.ctrl.content.AbstractMaskPerform;
import com.sqleo.environment.ctrl.content.MaskExport;
import com.sqleo.environment.ctrl.editor.Task;
import com.sqleo.environment.ctrl.editor._TaskSource;
import com.sqleo.environment.ctrl.editor._TaskTarget;
import com.sqleo.environment.mdi._ConnectionListener;


public class DataComparerCriteriaPane extends JPanel implements _ConnectionListener, ItemListener, _TaskSource{
	
	private JComboBox cbxConnection;
	private JComboBox cbxSchema;
	private JTextField txtTable;
	private Map<DATA_TYPE,DataComparerCriteriaDialogPane> dataTypePanelMap = 
		new HashMap<DATA_TYPE,DataComparerCriteriaDialogPane>(3);
	private String query;

	public DataComparerCriteriaPane(final String headerText){
		Application.window.addListener(this);
		
		setBackground(Color.white);
		setBorder(LineBorder.createGrayLineBorder());
		
		initComponents(headerText);
	}
	
	private void initComponents(final String headerText){
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		gbc.anchor = GridBagConstraints.NORTH;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.CENTER;
		final JLabel header = new JLabel(headerText);

		final Font font = new Font(header.getFont().getName(), Font.BOLD, header.getFont().getSize());
		final Map<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
		map.put(TextAttribute.FONT, font);
		map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		header.setFont(Font.getFont(map));
		
		gbl.setConstraints(header, gbc);
		add(header);

		gbc.insets = new Insets(5,5,5,5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;

		gbc.gridx = 0;
		gbc.gridy = 1;
		final JLabel useConnections = new JLabel("Use connection:");
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
		final JLabel schemas = new JLabel("Schema:");
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
		final JLabel table = new JLabel("Table:");
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
		gbl.setConstraints(query,gbc);
		add(query);

	}
	
	private class QueryDialog extends AbstractDialogConfirm{
		private JTextArea syntax;
		
		protected QueryDialog() {
			super(Application.window, I18n.getString("datacomparer.generatedQueryButton", "Generated query text"));
			
			syntax = new JTextArea();
			syntax.setWrapStyleWord(true);
			syntax.setEditable(true);
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
	
	private void setQuery(){
		final String cols =  dataTypePanelMap.get(DATA_TYPE.COLUMNS).getText();
		final String aggr =  dataTypePanelMap.get(DATA_TYPE.AGGREGATES).getText();
		final String filters =  dataTypePanelMap.get(DATA_TYPE.FILTERS).getText();
		
		final StringBuilder val = new StringBuilder();
		boolean selectAppended = false;
		final boolean columnsGiven = cols!=null && !cols.isEmpty();
		if(columnsGiven){
			val.append("SELECT ");
			selectAppended = true;
			val.append("\n"+cols);
		}
		final boolean aggregatesGiven = aggr!=null && !aggr.isEmpty();
		if(aggregatesGiven){
			if(!selectAppended){
				val.append("SELECT ");
				selectAppended = true;
			}else{
				val.append(",");
			}
			val.append("\n");
			int i = 1;
			for(String aggrSplitted : aggr.split(",")){
				val.append(aggrSplitted).append(" AS NB").append(i).append(",");
				i++;
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
		cbxConnection.addItem(keycah);
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
		ConnectionHandler ch = ConnectionAssistant.getHandler(ie.getItem().toString());
		if(ch == null)
		{
			cbxSchema.setModel(new DefaultComboBoxModel());
		}
		else
		{
			ArrayList schemas = (ArrayList)ch.getObject("$schema_names");
			cbxSchema.setModel(new DefaultComboBoxModel(schemas.toArray()));
		}
		
		cbxSchema.setEnabled(cbxSchema.getItemCount()>0);
	}
	
	public String getSchema(){
		return cbxSchema.getSelectedIndex() != -1 ? cbxSchema.getSelectedItem().toString() : null;
	}

	public String getTable(){
		return txtTable.getText();
	}
	
	public String[] getTableColumns(){
		return SQLHelper.getColumns(getHandlerKey(), getSchema(), getTable());
	}
	
	public String getDataType(final DATA_TYPE dataType){
		return dataTypePanelMap.get(dataType).getText();
	}

	@Override
	public String getHandlerKey() {
		return cbxSchema.getSelectedIndex() != -1 ? cbxConnection.getSelectedItem().toString() : null;
	}

	@Override
	public String getSyntax() {
		setQuery();
		return query;
	}
	
	public void retrieveData(final PrintStream stream){
		final _TaskTarget target = new _TaskTarget(){
			@Override
			public boolean continueRun() {
				return false;
			}
			@Override
			public void onTaskFinished(String message, boolean error) {
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
					while(rs.next()){
						vals = new Object[cols];
						for(int i=1; i<=cols;i++){
							vals[i-1] = rs.getString(i);
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
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		};
		new Task(this , target , 0).run();
	}
	

}
