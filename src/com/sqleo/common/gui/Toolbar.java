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

import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JToolBar;

public class Toolbar extends JToolBar
{
    public Toolbar(int orientation)
    {
        super(orientation);
        setFloatable(false);
    }
    
	public JButton add(Action a)
	{
		return (JButton)add(new JButton(a));
	}
    
	public AbstractButton add(AbstractButton a)
	{
		super.add(a);
		if(a.getIcon()!=null) a.setText(null);
		return a;
	}
    
    public void addSeparator()
    {
        if(this.getOrientation() == HORIZONTAL)
            super.addSeparator();
        else
            this.addSeparator(new Dimension(0,10));
    }
    
    public void addHorizontalGlue()
    {
    	add(Box.createHorizontalGlue());
    }
    
    public Action getAction(int idx)
    {
    	if(this.getComponentCount() > idx && this.getComponent(idx) instanceof AbstractButton)
    		return ((AbstractButton)this.getComponent(idx)).getAction();
    	
    	return null;
    }
}
