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

package com.sqleo.querybuilder;

import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.gui.BorderLayoutPanel;


public abstract class BaseMask extends BorderLayoutPanel
{
	protected QueryBuilder builder;	
	
	private boolean okpressed;	
	private String title;

	BaseMask(String title, QueryBuilder builder)
	{
		super(2,2);
		this.title = title;
		this.builder = builder;
	}
	
	protected abstract boolean onConfirm();
	protected abstract void onShow();
	
    public boolean showDialog()
    {
    	DialogConfirm dlg = null;
		
		if(SwingUtilities.getWindowAncestor(builder) instanceof Frame)
			dlg = new DialogConfirm((Frame)SwingUtilities.getWindowAncestor(builder));
		else if(SwingUtilities.getWindowAncestor(builder) instanceof Dialog)
			dlg = new DialogConfirm((Dialog)SwingUtilities.getWindowAncestor(builder));

		dlg.pack();
    	dlg.show();
    	
    	return okpressed;
    }
    
    class DialogConfirm extends AbstractDialogConfirm
    {
    	private DialogConfirm(Frame owner)
    	{
    		super(owner,BaseMask.this.title);
			getContentPane().add(BaseMask.this);
    	}
    	
    	private DialogConfirm(Dialog owner)
    	{
    		super((Dialog)owner,BaseMask.this.title);
			getContentPane().add(BaseMask.this);
    	}
    	
		public void show()
		{
			setLocationRelativeTo(BaseMask.this.builder);
			super.show();
		}
    	
		protected boolean onConfirm()
		{
			return BaseMask.this.okpressed = BaseMask.this.onConfirm();
		}

		protected void onOpen()
		{
			BaseMask.this.onShow();
		}    	
    }
}