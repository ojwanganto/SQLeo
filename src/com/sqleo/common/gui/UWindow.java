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
import java.awt.Window;

public class UWindow
{
	public static void centerOnScreen(Window window)
	{
		Dimension dimScreen = window.getToolkit().getScreenSize();
		Dimension dimThis = window.getSize();
		
		window.setLocation((dimScreen.width/2)-(dimThis.width/2), (dimScreen.height/2)-(dimThis.height/2));
	}
	
	public static void fullScreen(Window window)
	{
		Dimension dimScreen = window.getToolkit().getScreenSize();
		
		window.setLocation(-4,-4);
		window.setSize(dimScreen.width+8, dimScreen.height+8);
	}
}