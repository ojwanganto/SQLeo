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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.util.Classpath;
import com.sqleo.common.util.Text;
import com.sqleo.environment.Application;


public class MaskDriver extends JPanel
{
	private JTextField txtName;
	private JTextField txtFile;
	private JTextField txtExample;
	private JComboBox cbxDriver;
		
	MaskDriver()
	{
		setBorder(LineBorder.createGrayLineBorder());
			
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		addField(gbl,"name:"		,txtName = new JTextField()		,0);
		addField(gbl,"file:"		,txtFile = new JTextField()		,25);
		addField(gbl,"driver:"		,cbxDriver = new JComboBox()	,5);
		addField(gbl,"example:"		,txtExample = new JTextField()	,5);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor		= GridBagConstraints.WEST;
		gbc.gridwidth	= GridBagConstraints.REMAINDER;
		gbc.insets		= new Insets(25,8,0,8);
		
		txtFile.setEditable(false);
		cbxDriver.setEditable(true);
		cbxDriver.setPreferredSize(txtFile.getPreferredSize());
	}
	
	private void addField(GridBagLayout gbl, String text, JComponent txt, int top)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor		= GridBagConstraints.WEST;
		gbc.gridwidth	= GridBagConstraints.REMAINDER;
		gbc.insets		= new Insets(top,8,1,8);
				
		JLabel lbl = new JLabel(text);
		gbl.setConstraints(lbl, gbc);
		add(lbl);
		
		gbc.fill	= GridBagConstraints.HORIZONTAL;
		gbc.insets	= new Insets(0,8,0,8);
		gbc.weightx	= 1.0;
		
		gbl.setConstraints(txt, gbc);
		add(txt);
	}
	
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);
		for(int i=0; i<getComponentCount();i++)
			getComponent(i).setEnabled(b);
	}
	
	void addDriver(String classname)
	{
		cbxDriver.addItem(classname);
	}
	
	void clearDrivers()
	{
		cbxDriver.removeAllItems();
	}
	
	void load(UoDriver uoDv)
	{
		txtName.setText(uoDv.name);
		txtFile.setText(uoDv.library);
		txtExample.setText(uoDv.example);
		
		cbxDriver.setSelectedItem(uoDv.classname);
		if(cbxDriver.getSelectedIndex() == -1 && !Text.isEmpty(uoDv.classname))
			cbxDriver.addItem(uoDv.classname);
	}
		
	boolean unload(UoDriver uoDv)
	{
		if(Text.isEmpty(txtName.getText()) || cbxDriver.getSelectedItem()==null
		|| Text.isEmpty(cbxDriver.getSelectedItem().toString()))
		{
			Application.alert(Application.PROGRAM,"the values aren't complete!");
			return false;
		}
		
		if(txtName.getText().indexOf('.')!=-1
		|| txtName.getText().indexOf(',')!=-1
		|| txtName.getText().indexOf(';')!=-1
		|| txtName.getText().indexOf(':')!=-1
		|| txtName.getText().indexOf('|')!=-1
		|| txtName.getText().indexOf('/')!=-1
		|| txtName.getText().indexOf('<')!=-1
		|| txtName.getText().indexOf('>')!=-1
		|| txtName.getText().indexOf('@')!=-1
		|| txtName.getText().indexOf('\\')!=-1)
		{
			Application.alert(Application.PROGRAM,"characters \\.|,/;<:>@ aren't allowed!\nplease, enter a valid name.");
			return false;
		}
		
		try
		{
			String name = txtName.getText().trim();
			String file = txtFile.getText().trim();
			String classname = cbxDriver.getSelectedItem().toString().trim();
			
			ConnectionAssistant.declare(file,classname,!Classpath.isRuntime(file));
			
			if(uoDv.name!=null && !name.equals(uoDv.name))
			{
				/* change preferences ref */
				Application.session.mount(Application.ENTRY_PREFERENCES);
				
				Application.session.home();
				Application.session.jump("metaview." + uoDv.name);
				for(Enumeration e = Application.session.jumps(); e.hasMoreElements();)
				{
					String metaview = e.nextElement().toString();
					Iterator i = Application.session.jump(metaview).iterator();
					
					Application.session.home();
					Application.session.jump("metaview." + name);
					Application.session.jump(metaview);
					
					while(i.hasNext())
						Application.session.jump().add(i.next());
					
					Application.session.home();
					Application.session.jump("metaview." + uoDv.name);
				}
				Application.session.home();
				Application.session.ujump("metaview." + uoDv.name);
			}
			
			uoDv.name		= name;
			uoDv.library	= file;
			uoDv.classname	= classname;
			uoDv.example	= txtExample.getText().trim();
			
			return true;
		}
		catch(Exception e)
		{
			Application.println(e,true);
		}
		return false;		
	}
}