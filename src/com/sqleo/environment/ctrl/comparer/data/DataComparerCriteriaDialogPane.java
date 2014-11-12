/*
 *
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
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

package com.sqleo.environment.ctrl.comparer.data;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.comparer.data.DataComparerDialogTable.DATA_TYPE;

public class DataComparerCriteriaDialogPane extends BorderLayoutPanel{

	private JButton configure;
	private JTextArea syntax;
	private DataComparerCriteriaPane ownerPanel;
	
	private class ShowDialogTable extends AbstractAction{
		private final DataComparerDialog dialog;
		public ShowDialogTable(final DataComparerDialog dialog)	{
			super();
			this.dialog = dialog;
        	putValue(SMALL_ICON,Application.resources.getIcon(Application.ICON_PREFERENCES));
        	putValue(SHORT_DESCRIPTION,"configure");
		}
        
		public void actionPerformed(ActionEvent ae)	{
			dialog.addColumns(ownerPanel.getTableColumns());
			dialog.setVisible(true);
		}
	}
	
	public DataComparerCriteriaDialogPane(final DataComparerDialogTable.DATA_TYPE dataType, 
			final DataComparerCriteriaPane ownerPanel){
		
		this.ownerPanel = ownerPanel;
		final DataComparerDialog dialog = new DataComparerDialog(dataType, this);
		this.configure = new JButton(new ShowDialogTable(dialog));
		
		syntax = new JTextArea();
		syntax.setWrapStyleWord(true);
		syntax.setEditable(true);
		syntax.setLineWrap(true);
		syntax.setOpaque(true);
		syntax.setTabSize(4);
		syntax.setCaretPosition(0);
		syntax.setBackground(Color.white);
		if(DATA_TYPE.AGGREGATES == dataType){
			syntax.setText("count(*)");
		}
		
		final String title = dialog.getDataType().name().toLowerCase();
		setBorder(new TitledBorder(title));
		
		setComponentCenter(new JScrollPane(syntax));
		setComponentWest(configure);
		
	}
	
	public void setText(final String text){
		syntax.setText(text);
	}
	
	public String getText(){
		return syntax.getText();
	}
	
	
}
