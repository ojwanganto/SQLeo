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

package com.sqleo.environment.ctrl.editor;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.querybuilder.syntax.QueryTokens;


public abstract class BuildBasePane extends BorderLayoutPanel
{
	protected _BuildOwner owner;
	private JTextArea syntax;
	
	public BuildBasePane(_BuildOwner owner)
	{
		super(2,2);
		this.owner = owner;
		
		syntax = new JTextArea();
		syntax.setWrapStyleWord(true);
		syntax.setEditable(false);
		syntax.setLineWrap(true);
		syntax.setOpaque(false);
		syntax.setTabSize(4);
		syntax.setRows(3);
		
		JScrollPane scroll = new JScrollPane(syntax);
		scroll.setBorder(new TitledBorder(" syntax "));
		
		BorderLayoutPanel south = new BorderLayoutPanel();
		south.setComponentCenter(scroll);
		
		setComponentSouth(south);
		initComponents();
	}
	
	abstract void add(QueryTokens.Column column);
	abstract void clear();
	abstract void initComponents();
	abstract String getSyntax();
	
	public void fireQueryChanged()
	{
		syntax.setText(this.getSyntax());
	}
}