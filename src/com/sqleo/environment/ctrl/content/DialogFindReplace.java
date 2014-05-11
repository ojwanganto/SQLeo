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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.sqleo.common.gui.CommandButton;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.ContentPane;


public class DialogFindReplace extends JDialog implements ActionListener
{
	private CommandButton btnFind;
	private CommandButton btnReplace;
	private CommandButton btnReplaceAll;
	
	private JCheckBox chxAll;
	private JCheckBox chxNullFind;
	private JCheckBox chxNullReplace;
//	private JCheckBox chxBlock;
//	private JCheckBox chxChanges;
	private JCheckBox chxCase;
	
	private JComboBox cbxColumns;
	private JComboBox cbxOperator;
	
	private JTextField txtFind;
	private JTextField txtReplace;	
	
	private ContentFlag flag;
	private ContentView view;
	
	public DialogFindReplace(ContentPane content)
	{
		super(Application.window,"Find/replace");
		this.flag = new ContentFlag();
		this.view = content.getView();
				
		init();
		pack();
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(Application.window);
		setResizable(false);
	}
	
	public void dispose()
	{
		view.mark(null);
		super.dispose();		
	}

	private void init()
	{
		Vector vCols = new Vector();
		for(int i=0; i<view.getColumnCount(); i++)
			vCols.addElement(view.getColumnName(i));
		
		JPanel pnlT = new JPanel(new GridLayout(3,3,2,0));
		pnlT.add(new JLabel("Find into:"));
		pnlT.add(cbxColumns = new JComboBox(vCols));
		pnlT.add(chxAll	= new JCheckBox("all columns"));
		
		pnlT.add(cbxOperator = new JComboBox(new String[]{"equals","contains","starts with","ends with"}));
		pnlT.add(txtFind	 = new JTextField());
		pnlT.add(chxNullFind = new JCheckBox("null"));
		
		pnlT.add(new JLabel("Replace with:"));
		pnlT.add(txtReplace		= new JTextField());
		pnlT.add(chxNullReplace	= new JCheckBox("null"));
		
		JPanel pnlC = new JPanel(new GridLayout(1,3,2,0));
//		pnlC.add(chxBlock	= new JCheckBox("only current block"));
//		pnlC.add(chxChanges	= new JCheckBox("only changed cells"));
		pnlC.add(chxCase	= new JCheckBox("Case sensitive"));

		JPanel pnlB = new JPanel();
		pnlB.add(btnFind		= new CommandButton("Find",this));
		pnlB.add(btnReplace		= new CommandButton("Replace",this));
		pnlB.add(btnReplaceAll	= new CommandButton("Replace all",this));

		btnReplace.setEnabled(false);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 10, 0, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(pnlT,gbc);
		getContentPane().add(pnlC,gbc);
		getContentPane().add(pnlB,gbc);
		
		chxAll.addActionListener(this);
		chxNullFind.addActionListener(this);
		chxNullReplace.addActionListener(this);
	}

	private void mark(int row, int col)
	{
		flag.block = (row/ContentModel.MAX_BLOCK_RECORDS) + 1;
		flag.row = row - (ContentModel.MAX_BLOCK_RECORDS * (flag.block-1));
		flag.col = col;
				
		view.mark(flag);
	}
	
	private boolean find()
	{
		if(view.getRowCount() == 0) return false;
		
		int cStart = view.getColumn();
		if(cStart == -1) cStart = 0;
		
		int rStart = view.getFlatRow();
		if(rStart == -1) rStart = 0;
		
		if(!chxAll.isSelected())
		{
			cStart = view.getColumnIndex(cbxColumns.getSelectedItem().toString());
		}
		
		int col = cStart;
		int row = rStart;
		
		view.mark(null);
		
		do
		{
			if(chxAll.isSelected())
			{
				if(++col == view.getColumnCount())
				{
					col = 0;
					if(++row == view.getFlatRowCount())
						row = 0;
				}
			}
			else
			{
				if(++row == view.getFlatRowCount())
					row = 0;				
			}	
			
			Object value = view.getFlatValueAt(row,col);
			
			boolean bmark = false;
			if(value!=null)
			{
				String f = chxCase.isSelected() ? txtFind.getText() : txtFind.getText().toUpperCase();
				String v = chxCase.isSelected() ? value.toString() : value.toString().toUpperCase();
								
				if(cbxOperator.getSelectedItem().toString().equals("equals"))
				{
					bmark = v.equals(f);
				}
				else if(cbxOperator.getSelectedItem().toString().equals("contains"))
				{
					bmark = v.indexOf(f) != -1;
				}
				else if(cbxOperator.getSelectedItem().toString().equals("starts with"))
				{
					bmark = v.startsWith(f);
				}
				else if(cbxOperator.getSelectedItem().toString().equals("ends with"))
				{
					bmark = v.endsWith(f);
				}
			}
			else
			{
				bmark = chxNullFind.isSelected();
			}
			
			if(bmark)
			{
				mark(row,col);				
				return true;
			}
		}
		while(!(col==cStart && row==rStart));
		Application.alert(Application.PROGRAM,"Value not found!");
		return false;
	}
	
	private boolean replace()
	{
		Object value = txtReplace.getText();
		if(chxNullReplace.isSelected()) value = null;
		
		view.setValueAt(value,flag.row,flag.col);
		view.mark(null);
		
		return find();
	}

	public void actionPerformed(ActionEvent ae)
	{
		cbxColumns.setEnabled(!chxAll.isSelected());
		cbxOperator.setEnabled(!chxNullFind.isSelected());
		txtFind.setEnabled(!chxNullFind.isSelected());
		txtReplace.setEnabled(!chxNullReplace.isSelected());
		
		if(ae.getSource() == btnFind)
		{
			btnReplace.setEnabled(find());
		}
		else if(ae.getSource() == btnReplace)
		{
			btnReplace.setEnabled(replace());			
		}
		else if(ae.getSource() == btnReplaceAll)
		{
			btnReplace.setEnabled(false);
			while(replace());
		}
	}
}
