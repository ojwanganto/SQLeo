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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.metal.MetalBorders;

import com.sqleo.environment.Preferences;

public class ClientFrame extends JInternalFrame
{
    public ClientFrame(String title)
    {
		super(title);
		setSize(640,480);
		setBorder(new InternalBorder());
		setContentPane(new BorderLayoutPanel());
    }
    
    public final void setContentPane(Container container)
    {
		if(container.getLayout() instanceof BorderLayout)
		    super.setContentPane(container);
		else
		    throw new IllegalArgumentException();
    }

    public final void setComponentNorth(Component comp)
	{
	    this.getContentPane().add(comp, BorderLayout.NORTH);
	}
	
    public final void setComponentSouth(Component comp)
	{
	    this.getContentPane().add(comp, BorderLayout.SOUTH);
	}

    public final void setComponentCenter(Component comp)
	{
	    this.getContentPane().add(comp, BorderLayout.CENTER);
	}
	
    public final void setComponentEast(Component comp)
	{
	    this.getContentPane().add(comp, BorderLayout.EAST);
	}
	
    public final void setComponentWest(Component comp)
	{
	    this.getContentPane().add(comp, BorderLayout.WEST);
	}
    
    public void setUI(InternalFrameUI internalframeui)
	{
        super.setUI(internalframeui);
        setCaptionHeight(20);
    }

    public boolean setCaptionHeight(int nHeight)
	{
    	nHeight = Preferences.getScaledRowHeight(nHeight);

        InternalFrameUI internalframeui = getUI();
        
		if(!(internalframeui instanceof BasicInternalFrameUI))
            return false;
		
        JComponent comTitle = ((BasicInternalFrameUI)internalframeui).getNorthPane();
        
		if(comTitle == null)
            return false;
		
        Dimension dimTitle = comTitle.getPreferredSize();
        dimTitle.height = nHeight;
        comTitle.setPreferredSize(dimTitle);
			
        return true;
    }
    
    private class InternalBorder extends MetalBorders.InternalFrameBorder
    {
        public Insets getBorderInsets(Component c)
        {
            return new Insets(2,2,2,2);
        }
    }
}