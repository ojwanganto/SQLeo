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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class CustomLineBorder extends AbstractBorder
{
	protected Insets borderInsets = new Insets( 2, 2, 2, 2 );
	
    protected int thickness		= 1;
    protected Color lineColor	= new Color(153,153,153,153);
	
	protected boolean left, right, top, bottom;
	
	public CustomLineBorder()
	{
		this(true,true,true,true);
	}
	
	public CustomLineBorder(boolean top, boolean left, boolean bottom, boolean right)
	{
		this.top	= top;
		this.left	= left;
		this.bottom = bottom;
		this.right	= right;
	}
	
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        Color oldColor = g.getColor();
        g.setColor(lineColor);
        
        for(int i = 0; i < thickness; i++)
        {
			if(top)
				g.drawLine(x+i, y+i, width-i-i-1, y+i);
			
			if(bottom)
				g.drawLine(x+i, y+height-i-i-1, width-i-i-1, y+height-i-i-1);
			
			if(left)
				g.drawLine(x+i, y+i, x+i, y+height-i-i-1);
			
			if(right)
				g.drawLine(x+width-i-i-1, y+i, x+width-i-i-1, y+height-i-i-1);
        }
        
        g.setColor(oldColor);
    }

    public Insets getBorderInsets(Component c)
    {
		return borderInsets;
    }
}
