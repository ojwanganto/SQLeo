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

package com.sqleo.common.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.sqleo.common.util.I18n;


public abstract class AbstractDialogConfirm extends AbstractDialogModal
{
	protected CommandButton btnConfirm;
	
	protected AbstractDialogConfirm(Dialog owner, String title)
	{
		super(owner,title);
	}	
	
	protected AbstractDialogConfirm(Component owner, String title)
	{
		this(owner, title, INITIAL_WIDTH, INITIAL_HEIGHT);
	}
	
	protected AbstractDialogConfirm(Component owner, String title, int width, int height)
	{
		super(owner, title, width, height);
	}
	
	protected void initComponent(Component owner, String title, Dimension size)
	{
		super.initComponent(owner, title, size);
		btnConfirm = insertButton(1,I18n.getString("application.ok","Ok"));

		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{	
				ae = new ActionEvent(	AbstractDialogConfirm.this.btnConfirm,
										ActionEvent.ACTION_PERFORMED,
										AbstractDialogConfirm.this.btnConfirm.getText());
				
				AbstractDialogConfirm.this.actionPerformed(ae);
			}
		};

		this.getRootPane().registerKeyboardAction(al, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);		
	}
	
	protected abstract boolean onConfirm();
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnConfirm)
		{
			if(!onConfirm()) return;
		}
		
		super.actionPerformed(ae);
	}
}
