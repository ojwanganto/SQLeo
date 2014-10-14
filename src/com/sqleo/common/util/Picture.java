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

package com.sqleo.common.util;

import java.awt.Image;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.sqleo.environment.Preferences;
import com.sqleo.environment.mdi.DialogPreferences;

public class Picture
{
	
	public static Icon createIcon(String filename)
	{
		Class c = Resources.class;
		if(filename!=null)
		    return createIcon(c.getResource(filename));
		
		return null;
	}
	
	public static Icon createIcon(URL url)
	{
		final ImageIcon original = new ImageIcon(url);
        final int iconSizePercentage = Preferences.getInteger(DialogPreferences.ICON_SIZE_PERCENTAGE, DialogPreferences.DEFAULT_ICON_PERCENT);
        if (iconSizePercentage != 100) {
        	final float multiplier = iconSizePercentage / 100.0f;
			final Image img = original.getImage();  
			final Image scaled = img.getScaledInstance(Math.round(original.getIconWidth() * multiplier) , Math.round(original.getIconHeight() * multiplier) ,  
					java.awt.Image.SCALE_SMOOTH);  
			return new ImageIcon(scaled);
		}else{
			return original;
		}
	}
}
