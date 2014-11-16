/*
 *
 * Modified by SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
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

package com.sqleo.environment.ctrl.define;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.ListView;
import com.sqleo.environment.Application;


public class ColumnsChooser extends AbstractDialogConfirm implements ItemListener
{
	private JComboBox listviews;
	private JList columns;
	
	private ColumnsChooser(String dvname,String defaultmetaview)
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

	public static void showDialog(String dvname,String defaultmetaview)
	{
		ColumnsChooser dlg = new ColumnsChooser(dvname,defaultmetaview);
		dlg.setVisible(true);
	}
	
	private static void list(String dvname,String metaview,ListView lv,
			ResultSet rs,ResultSetMetaData rsmd,ArrayList rows)throws SQLException{

		Application.session.mount(Application.ENTRY_PREFERENCES);
		Application.session.home();
		Application.session.jump("metaview." + dvname);
		
		ArrayList cols = Application.session.jump(metaview);
		lv.reset();
		
		if(cols.size() == 0)
		{
			for(int i=1; i<=rsmd.getColumnCount(); i++)
			{
// Ticket #250			String name = rsmd.getColumnName(i);
				String name = rsmd.getColumnLabel(i);
				lv.addColumn(name);
				
				cols.add(new Object[]{name,new Boolean(true)});
			}
			
		}
		else
		{
			for(int i=0; i<cols.size(); i++)
			{
				Object[] col = (Object[])cols.get(i);
				if(((Boolean)col[1]).booleanValue())
					lv.addColumn(col[0].toString());
			}
		}
		if(rs!=null){
			while(rs.next())
			{
				Object[] rowdata = new Object[cols.size()];
				for(int i=0,j=0; i<rowdata.length; i++)
				{
					Object[] col = (Object[])cols.get(i);
					if(((Boolean)col[1]).booleanValue())
					{
						rowdata[j++] = rs.getObject(i+1);
						if(rowdata[j-1]!=null && rowdata[j-1] instanceof String)
							rowdata[j-1] = rowdata[j-1].toString().trim();
					}
				}
				lv.addRow(rowdata);
			}
			
			rs.close();
		}else{
			for(Object row : rows){
				lv.addRow((String[])row);
			}
		}
		

	} 
	
	public static void list(String dvname,String metaview,ListView lv,ResultSetMetaData rsmd,ArrayList rows)
	throws SQLException	{
		list(dvname,metaview,lv,null,rsmd,rows);
	}
	
	public static void list(String dvname,String metaview,ListView lv,ResultSet rs) throws SQLException	{
		list(dvname,metaview,lv,rs,rs.getMetaData(),null);
	}	
}