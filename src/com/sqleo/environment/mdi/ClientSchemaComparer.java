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

package com.sqleo.environment.mdi;

import javax.swing.JMenuItem;
import javax.swing.border.EmptyBorder;

import com.sqleo.common.gui.Toolbar;
import com.sqleo.environment.ctrl.SchemaComparer;


public class ClientSchemaComparer extends MDIClient
{
	public static final String DEFAULT_TITLE = "Schema comparer";
	
	private SchemaComparer control;
	private JMenuItem[] m_actions;
	
	ClientSchemaComparer()
	{
		super(DEFAULT_TITLE);
		
		setComponentCenter(control = new SchemaComparer());
		control.setBorder(new EmptyBorder(2,2,2,2));
		
		initMenuActions();
	}
    
	private void initMenuActions()
	{
		m_actions = new JMenuItem[]
		{
			new JMenuItem("<empty>")
		};
		m_actions[0].setEnabled(false);
	}
	
	public JMenuItem[] getMenuActions()
	{
		return m_actions;
	}

	public Toolbar getSubToolbar()
	{
		return null;
	}
    
	public final String getName()
	{
		return DEFAULT_TITLE;
	}

	protected void setPreferences()
	{
	}
}
