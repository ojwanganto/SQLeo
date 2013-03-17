/*
 *
 * Modified by SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
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

import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sqleo.common.gui.AbstractDialogModal;
import com.sqleo.environment.Application;


public class DialogAbout extends AbstractDialogModal
{
	public DialogAbout()
	{
		super(Application.window, Application.PROGRAM + ".about [" + Application.WEB + "]");
		setResizable(false);
		
		JTextArea txt = new JTextArea();
		txt.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder(), new EmptyBorder(2,2,2,2)));
		txt.setEditable(false);
		txt.setOpaque(false);
		txt.setLineWrap(true);
		txt.setWrapStyleWord(true);
		
		txt.append("SQLeo Visual Query Builder :: java database frontend\n");
		txt.append("Version : "+Application.MAJOR + "." +Application.MINOR + "\n");
		txt.append("Copyright (C) "+Application.MAJOR + Application.AUTHOR + "\n");
		txt.append("Project website: "+Application.WEB+"\n");
		txt.append("\n");
		txt.append("This program is free software; you can redistribute it and/or\n");
		txt.append("modify it under the terms of the GNU General Public License\n");
		txt.append("as published by the Free Software Foundation; either version 2\n");
		txt.append("of the License, or (at your option) any later version.\n");
		txt.append("\n");
		txt.append("This program is distributed in the hope that it will be useful,\n");
		txt.append("but WITHOUT ANY WARRANTY; without even the implied warranty of\n");
		txt.append("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n");
		txt.append("GNU General Public License for more details.\n");
		txt.append("\n");
		txt.append("You should have received a copy of the GNU General Public License\n");
		txt.append("along with this program; if not, write to the Free Software\n");
		txt.append("Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.\n");

		getContentPane().add(txt);
	}

	protected void onOpen()
	{
	}
}
