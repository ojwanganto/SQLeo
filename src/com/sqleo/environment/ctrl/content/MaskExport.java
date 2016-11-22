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

package com.sqleo.environment.ctrl.content;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.JdbcUtils;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.common.util.Text;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.ContentPane;


public class MaskExport extends AbstractMaskPerform
{
	public AbstractChoice eChoice;
	public ResultSet rs = null;
	private Statement stmt = null;

	public void setEnabled(boolean b)
	{
		super.setEnabled(b);
		for(int i=0; i<eChoice.getComponentCount();i++)
			eChoice.getComponent(i).setEnabled(b);
	}
	
	public void setType(short type, String tname, String fname, boolean appendExtension)
	{
		if(eChoice!=null) remove(eChoice);
		
		progress.setValue(0);
		progress.setMaximum(0);
		
		if(type == WEB)
		{
			if(!fname.endsWith(".htm") && !fname.endsWith(".html")) fname = fname + ".html"; 
			setComponentCenter(eChoice = new WebChoice());
		}
		else if(type == SQL)
		{
			if(!fname.endsWith(".sql")) fname = fname + ".sql"; 
			setComponentCenter(eChoice = new SqlChoice(tname));
		}
		else if(type == TXT)
		{
			if(!fname.endsWith(".txt")) fname = fname + ".txt"; 
			setComponentCenter(eChoice = new TxtChoice());
		}
		else if(type == CSV)
		{
			if(appendExtension && !fname.endsWith(".csv")) fname = fname + ".csv";
			TxtChoice csvChoice = new TxtChoice();
			// csvChoice.setDefaultDelimiter(",");
			setComponentCenter(eChoice = csvChoice);
		}
			
		lblFile.setText("file: " + fname);
	}	
//	-----------------------------------------------------------------------------------------
//	?????????????????????????????????????????????????????????????????????????????????????????
//	-----------------------------------------------------------------------------------------
	void init()
	{
		super.init();
		
		progress.setValue(0);
		progress.setMaximum(eChoice.getLastRow() - eChoice.getFirstRow() + 1);

		if(!eChoice.isExportFromGrid()){
			executeContentViewQuery();
			if(rs!=null){
				try {
					eChoice.open(rs.getMetaData());
				} catch (SQLException e) {
					Application.println(e, true);
				}
			}
		}else{
			eChoice.open();
		}
	}

	public void export(){
		if(null == rs){
			return;
		}
		try {
			int cols= rs.getMetaData().getColumnCount();
			Object[] vals = null;
			while(rs!=null && rs.next()){
				vals = new Object[cols];
				for(int i=1; i<=cols;i++)				{
					vals[i-1] = SQLHelper.getRowValue(rs, i);
				}
				eChoice.handle(vals);
			}
		} catch (SQLException e) {
			Application.println(e, true);
		}finally {
			try {
				if(rs!=null){
					rs.close();
				}
			} catch (SQLException e) {
				Application.println(e, true);
			}
		}
		progress.setValue(progress.getMaximum());

	}

	@Override
	protected void fireOnBtnStopClicked() {
		JdbcUtils.cancelAndCloseStatement(stmt);
		stmt = null;
		rs = null;
	}
	
	private void executeContentViewQuery(){
		try
		{
			stmt = null;
			ContentPane target = view.getControl();
			String syntax = target.getQuery();
			if(target.getHandlerKey()!=null){
				ConnectionHandler ch = ConnectionAssistant.getHandler(target.getHandlerKey());
				stmt = ch.get().createStatement();
				// ticket #375 prevent OutOfMemory errors with Big MySQL tables
				if( ch.getDatabaseProductName() == "MySQL")
				{
					stmt.setFetchSize(Integer.MIN_VALUE); // MySQL streaming row by row
				} else {
					stmt.setFetchSize(1000);
				}

				syntax = SQLHelper.getSQLeoFunctionQuery(syntax,target.getHandlerKey());
				rs = JdbcUtils.executeQuery(ch, syntax, stmt);
			}
		}
		catch(SQLException sqle)
		{
			Application.println(sqle,true);
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					Application.println(sqle,true);
				}
			}
		} 
	}


	void next()
	{
		eChoice.handle(view.getFlatValues(progress.getValue() + eChoice.getFirstRow() - 1));
		progress.setValue(progress.getValue()+1);
	}
	
	boolean finished()
	{
		if(progress.getValue() == progress.getMaximum())
		{
			eChoice.close();
			
			btnStop.setEnabled(false);
			lblMsg.setText("Ready!");
			
			return true;
		}
		
		return false;
	}
	
	public boolean isExportFromGrid(){
		return eChoice.isExportFromGrid();
	}
	
//	-----------------------------------------------------------------------------------------
//	-----------------------------------------------------------------------------------------
	private abstract class AbstractChoice extends BorderLayoutPanel
	{
		private PrintStream stream;
		
		JRadioButton rbAll;
		JRadioButton rbUser;
		
		JTextField txtInterval;

		private JCheckBox cbxFromGrid;

		AbstractChoice()
		{
			setBorder(new TitledBorder("Options"));
			initComponents();
		}
		
		public boolean isExportFromGrid(){
			return cbxFromGrid.isSelected();
		}

		abstract void open(ResultSetMetaData metaData);

		void initComponents()
		{
			JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
			setComponentSouth(pnl);
			pnl.add(cbxFromGrid = new JCheckBox("From grid"));
			cbxFromGrid.setSelected(false);
			pnl.add(new JLabel("records:"));
			pnl.add(rbAll	= new JRadioButton("All",true));
			pnl.add(rbUser	= new JRadioButton("Define:"));
			pnl.add(txtInterval = new JTextField("1..",8));
			txtInterval.setEditable(false);
			txtInterval.setEnabled(false);
			
			ButtonGroup bg = new ButtonGroup();
			ItemListener il = new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					txtInterval.setEditable(rbUser.isSelected());
					txtInterval.setEnabled(rbUser.isSelected());
					
					if(rbAll.isSelected())
					{
						txtInterval.setText("1..");
					}
				}
			};
			
			bg.add(rbAll);			
			bg.add(rbUser);
								
			rbAll.addItemListener(il);
			rbUser.addItemListener(il);
		}

		int getFirstRow()
		{
			String interval = eChoice.txtInterval.getText();
			int pos = interval.indexOf("..");
						
			return Integer.valueOf(interval.substring(0,pos)).intValue();
		}
		
		int getLastRow()
		{
			String interval = eChoice.txtInterval.getText();
			int pos = interval.indexOf("..") + 2;
			
			return pos < interval.length() ? Integer.valueOf(interval.substring(pos)).intValue() : view.getFlatRowCount();
		}
		
		void open()
		{
			open(false);
		}
		
		void open(final boolean append)
		{
			try
			{
				// Ticket #45 this use default encoding, can be modified when launching app by
				// java -Dfile.encoding=UTF-8 -jar SQLeoVQB.jar 
				stream = new PrintStream(new FileOutputStream(MaskExport.this.lblFile.getText().substring(6), append));
			}
			catch (FileNotFoundException e)
			{
				Application.println(e,true);
			}
		}
		
		abstract void handle(Object[] vals);
		
		void close()
		{
			stream.close();
		}
		
		void print(String s)
		{
			stream.print(s);
		}
		
		void println(String s)
		{
			stream.println(s);
		}
	}

	private class WebChoice extends AbstractChoice
	{
		JCheckBox cbxHeader;
		
		void initComponents()
		{
			JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnl.add(cbxHeader = new JCheckBox("With header"));
			setComponentCenter(pnl);
			
			super.initComponents();
		}

		void open(ResultSetMetaData mtd)
		{
			super.open();
			println("<html><body><table border=1>");

			if(cbxHeader.isSelected())
			{
				print("<tr>");
				try {
					for(int col=1; col<=mtd.getColumnCount(); col++)
					{
						// Ticket #171 replace getColumnName per getColumnLabel
						print("<th>" + Text.escapeHTML(mtd.getColumnLabel(col)) + "</th>");
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				println("</tr>");
			}
		}


		void handle(Object[] vals)
		{
			print("<tr>");
			for(int i=0; i<vals.length; i++)
			{
				String val = vals[i] == null ? "null" : vals[i].toString();
				print("<td>" + Text.escapeHTML(val) + "</td>");
			}		
			println("</tr>");
		}

		void close()
		{
			println("</table></body></html>");
			super.close();
		}
	}

	private class SqlChoice extends AbstractChoice
	{
		JCheckBox cbxDelete;
		JTextField txtTable;
		
		String insert = null;
		
		SqlChoice(String tname)
		{
			super();
			txtTable.setText(tname);
		}
		
		void initComponents()
		{
			JPanel pnl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnl1.add(new JLabel("Table name:"));
			pnl1.add(txtTable = new JTextField(10));			
			
			cbxDelete = new JCheckBox("With delete statement");
			
			JPanel pnl2 = new JPanel(new GridLayout(2,1));
			pnl2.add(pnl1);
			pnl2.add(cbxDelete);

			JPanel pnl3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			setComponentCenter(pnl3);
			pnl3.add(pnl2);

			super.initComponents();
		}

		void open(ResultSetMetaData mtd)
		{
			super.open();
			if(cbxDelete.isSelected())
			{
				println("DELETE FROM " + txtTable.getText() + ";");
			}

			StringBuffer buffer = new StringBuffer("INSERT INTO " + txtTable.getText() + " (");
			try {

				for(int col=1; col<=mtd.getColumnCount(); col++)
				{
					// Ticket #171 replace getColumnName per getColumnLabel
					buffer.append(mtd.getColumnLabel(col) + ",");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer.deleteCharAt(buffer.length()-1);
			insert = buffer.toString() + ")";
		}

		void handle(Object[] vals)
		{
			StringBuffer buffer = new StringBuffer();
			for(int i=0; i<vals.length; i++)
			{
//				String val = vals[i] == null ? "null" : vals[i].toString();
				buffer.append(toSQLValue(vals[i],i) + ",");
			}
			buffer.deleteCharAt(buffer.length()-1);		
			println(insert + " VALUES (" + buffer.toString() + ");");
		}
		
		private String toSQLValue(Object value,int col)
		{
			if(value==null) return "null";
		
			switch(MaskExport.this.view.getColumnType(col))
			{
				case Types.CHAR:
				case Types.VARCHAR:
				case Types.LONGVARCHAR:
				case Types.LONGNVARCHAR:
				case Types.NCHAR:
				case Types.NVARCHAR:
					value = Text.replaceText(value.toString(),"\'","\\\'");
					return "'" + value.toString() + "'";
				case Types.DATE:
					return "{d '" + value.toString() + "'}";
				case Types.TIME:
					return "{t '" + value.toString() + "'}";
				case Types.TIMESTAMP:
					return "{ts '" + value.toString() + "'}";
				default:
					return value.toString();
			}
		}
	}

	public class TxtChoice extends AbstractChoice
	{
		public JCheckBox cbxHeader;
		public JCheckBox cbxNull;
		public JCheckBox cbxCote;
		public int bytes;
		public int rowcount;
		
		JRadioButton rbTab;
		JRadioButton rbOther;
		
		JTextField txtDelimiter;
		ResultSetMetaData mtd;
		
		void initComponents()
		{
			JPanel pnl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnl1.add(cbxHeader = new JCheckBox("With header"));
			pnl1.add(cbxNull = new JCheckBox("Empty if null"));
			pnl1.add(cbxCote = new JCheckBox("Quote text"));
			
			JPanel pnl2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnl2.add(new JLabel("Delimiter:"));
			pnl2.add(rbTab = new JRadioButton("Tab",true));
			pnl2.add(rbOther = new JRadioButton("Other"));
			pnl2.add(txtDelimiter = new JTextField(";",5));
			txtDelimiter.setEditable(false);
			txtDelimiter.setEnabled(false);

			JPanel pnl3 = new JPanel(new GridLayout(2,1));
			pnl3.add(pnl1);
			pnl3.add(pnl2);

			JPanel pnl4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			setComponentCenter(pnl4);
			pnl4.add(pnl3);
			
			ButtonGroup bg = new ButtonGroup();
			bg.add(rbTab);
			bg.add(rbOther);

			rbTab.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					txtDelimiter.setEditable(!rbTab.isSelected());
					txtDelimiter.setEnabled(!rbTab.isSelected());
				}
			});
			
			super.initComponents();			
		}
		
		private String getDelimiter()
		{
			if(rbTab.isSelected()) return "\t";
			return txtDelimiter.getText();
		}
		private void disableDelimiterSelection(){
			rbTab.setEnabled(false);
			rbOther.setEnabled(false);
			txtDelimiter.setEnabled(false);
		}
		public void setTabAsDefaultAndDisable(){
			rbTab.setSelected(true);
			disableDelimiterSelection();
		}
		public void setDefaultDelimiter(final String delimiter){
			rbOther.setSelected(true);
			txtDelimiter.setText(delimiter);
		}
		
		public void close(){
			super.close();
		}
		
		void open(ResultSetMetaData mtd)
		{
			open(mtd, false);
		}

		public void open(ResultSetMetaData mtd,final boolean append)
		{
			this.bytes = 0;
			this.rowcount = 0;
			this.mtd = mtd;
			
			super.open(append);

			if(cbxHeader.isSelected())
			{
				StringBuffer buffer = new StringBuffer();
				try {
					for(int col=1; col<=mtd.getColumnCount(); col++)
					{
						// Ticket #171 replace getColumnName per getColumnLabel
						buffer.append(mtd.getColumnLabel(col) + getDelimiter());

					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(buffer.length() > 0) buffer.deleteCharAt(buffer.length()-1);
				println(buffer.toString());
			}
		}


		void handle(Object[] vals)
		{

			this.rowcount++;
			StringBuffer buffer = new StringBuffer();
			for(int i=0; i<vals.length; i++)
			{
				// Ticket #165 empty string if NULL
				if(vals[i]==null) {
					if(cbxNull.isSelected()) 
						vals[i]="";
					else
						vals[i]="null";

				// Ticket #162 enclose CHAR and VARCHAR with ""
				} else if (cbxCote.isSelected()) {
					final int columnType;
					if(MaskExport.this.view!=null){
						columnType = MaskExport.this.view.getColumnType(i);
					}else if(mtd != null){
						try {
							columnType = mtd.getColumnType(i+1);
						} catch (SQLException e) {
							e.printStackTrace();
							return;
						}
					}else{
						return;
					}
					switch(columnType)
					{
						case Types.CHAR:
						case Types.VARCHAR:
						case Types.LONGVARCHAR:
						case Types.LONGNVARCHAR:
						case Types.NCHAR:
						case Types.NVARCHAR:
							vals[i] = Text.replaceText(vals[i].toString(),"\"","\"\"");
							vals[i] = "\"" + vals[i].toString() + "\"";
						default:
							vals[i] = vals[i].toString();
					}
				} else {
					vals[i] = vals[i].toString();
				}
				buffer.append(vals[i] + getDelimiter());
			}
			if(buffer.length() > 0) buffer.deleteCharAt(buffer.length()-1);
			final String out = buffer.toString(); 
			bytes += out.length();
			println(out);
			
		}
	}
}
