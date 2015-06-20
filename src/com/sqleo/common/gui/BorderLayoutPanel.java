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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.sqleo.environment.Preferences;

public class BorderLayoutPanel extends JPanel
{
	public BorderLayoutPanel()
	{
		this(0,0);
	}

	public BorderLayoutPanel(int hgap, int vgap)
	{
		super(new BorderLayout(hgap,vgap));
	}
	
	public final void add(Component comp, Object constraints)
	{
		if(!(this.getLayout() instanceof BorderLayout))
			super.setLayout(new BorderLayout());
		
		super.add(comp,constraints);
	}

	public final void setLayout(LayoutManager mgr)
	{
		if(!(mgr instanceof BorderLayout))
			throw new IllegalArgumentException("This is a BorderLayout panel");
		
		super.setLayout(mgr);
	}
	
	public final void setComponentNorth(Component comp)
	{
		add(comp, BorderLayout.NORTH);
	}
	
	public final void setComponentSouth(Component comp)
	{
		add(comp, BorderLayout.SOUTH);
	}

	public final void setComponentCenter(Component comp)
	{
		add(comp, BorderLayout.CENTER);
	}
	
	public final void setComponentEast(Component comp)
	{
		add(comp, BorderLayout.EAST);
	}
	
	public final void setComponentWest(Component comp)
	{
		add(comp, BorderLayout.WEST);
	}
	
	@Override
	public void setSize(int w, int h) {
		super.setSize(Preferences.getScaledRowHeight(w),Preferences.getScaledRowHeight(h));
	}
}