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

package com.sqleo.environment.ctrl.explorer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.environment.Application;


public class DialogChooseColumns extends AbstractDialogConfirm implements ItemListener
{
	private JComboBox listviews;
	private JList columns;
	
	public DialogChooseColumns(String dvname,String defaultmetaview)
	{
		super(Application.window,dvname + " - choose columns",350,275);
		
		Application.session.mount(Application.ENTRY_PREFERENCES);
		Application.session.home();
		Application.session.jump("metaview." + dvname);
		
		Vector vmv = new Vector();
		for(Enumeration e = Application.session.jumps(); e.hasMoreElements();)
			vmv.addElement(e.nextElement());
		
		Object[] omv = vmv.toArray(); 
		Arrays.sort(omv);
		
		BorderLayoutPanel mask = new BorderLayoutPanel();
		mask.setComponentNorth(listviews = new JComboBox(omv));
		mask.setComponentCenter(new JScrollPane(columns = new JList()));
		
		listviews.addItemListener(this);
		listviews.setSelectedItem(defaultmetaview);
		
		getContentPane().add(mask);
		
//		insertButton(2,"apply");
	}
	
	protected boolean onConfirm()
	{
		if(listviews.getSelectedItem() == null) return false;
		
		String metaview = listviews.getSelectedItem().toString();
		String dvname = this.getTitle().substring(0,this.getTitle().lastIndexOf('-')-1);
		
		Application.session.mount(Application.ENTRY_PREFERENCES);
		Application.session.home();
		Application.session.jump("metaview." + dvname);
		
		ArrayList al = Application.session.jump(metaview);
		for(int i = 0; i<al.size(); i++)
		{
			Object[] col = (Object[])al.get(i);
			if(metaview.equals("table types") && (col[0].equals("TABLE_NAME") || col[0].equals("TABLE_TYPE")))
				col[1] = new Boolean(true);
			else
				col[1] = new Boolean(columns.isSelectedIndex(i));				
		}
		
		return true;
	}

	protected void onOpen()
	{
		itemStateChanged(null);
	}

	public void itemStateChanged(ItemEvent ie)
	{
		if(listviews.getSelectedItem() == null) return;
		
		String metaview = listviews.getSelectedItem().toString();
		String dvname = this.getTitle().substring(0,this.getTitle().lastIndexOf('-')-1);
		
		Application.session.mount(Application.ENTRY_PREFERENCES);
		Application.session.home();
		Application.session.jump("metaview." + dvname);
		
		ArrayList al = Application.session.jump(metaview);
		Vector vColumns = new Vector();
		
		for(int i = 0; i<al.size(); i++)
		{
			Object[] col = (Object[])al.get(i);
			vColumns.addElement(col[0].toString());
		}
		
		columns.setListData(vColumns);
		
		for(int i = 0; i<al.size(); i++)
		{
			Object[] col = (Object[])al.get(i);
			if(((Boolean)col[1]).booleanValue())
				columns.addSelectionInterval(i,i);
		}
	}
}