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

package com.sqleo.environment.ctrl.explorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.gui.ListView;
import com.sqleo.environment.Application;
import com.sqleo.environment.mdi.DefaultMaskChooser;


public class DialogExport extends AbstractDialogConfirm
{
	private ListView lv;
	private DefaultMaskChooser mkc;
	
	public DialogExport(ListView lv)
	{
		super(Application.window,"export list");
		this.lv = lv;
	}

	protected boolean onConfirm()
	{
		File file = mkc.getSelectedFile();
		if(file == null) return false;
		
		try
		{
			PrintStream out = new PrintStream(new FileOutputStream(file));
			
			StringBuffer header = new StringBuffer("| ");
			StringBuffer divider = new StringBuffer("+-");
		
			int columnDisplaySize[] = new int[lv.getColumnCount()];
			for(int row=0; row<lv.getRowCount(); row++)
			{
				for(int col=0; col<lv.getColumnCount(); col++)
				{
					Object value = lv.getValueAt(row,col);
					if(value==null) value = new String();
					
					if(value.toString().length() > columnDisplaySize[col])
						columnDisplaySize[col] = value.toString().length();					
				}
			}
			
			for(int col=0; col<lv.getColumnCount(); col++)
			{
				header.append(lv.getColumnName(col));
			
				char[] filler = new char[lv.getColumnName(col).length()];
				Arrays.fill(filler, '-');
				divider.append(filler);

				int diff = columnDisplaySize[col] - lv.getColumnName(col).length();
				if(diff > 0)
				{
					filler = new char[diff];
					Arrays.fill(filler, ' ');
					header.append(filler);

					Arrays.fill(filler, '-');
					divider.append(filler);					
				}
				else
				{
					columnDisplaySize[col] = lv.getColumnName(col).length();
				}
				
				header.append(" | ");
				divider.append("-+-");
			}
			divider.deleteCharAt(divider.length()-1);
			header.deleteCharAt(header.length()-1);
		
			out.println(divider.toString());
			out.println(header.toString());
			out.println(divider.toString());
			out.flush();
		
			for(int row=0; row<lv.getRowCount(); row++)
			{
				StringBuffer line = new StringBuffer("| ");
				for(int col=0; col<lv.getColumnCount(); col++)
				{
					Object value = lv.getValueAt(row,col);
					if(value==null) value = new String();
				
					int diff = columnDisplaySize[col] - value.toString().length();
					if(diff > 0)
					{
						char[] filler = new char[diff];
						Arrays.fill(filler, ' ');
						line.append(value + new String(filler));
					}
					else if(diff < 0)
					{
						value = value.toString().substring(0, columnDisplaySize[col]-3);
						line.append(value + "...");
					}
					else
						line.append(value);
				
					line.append(" | ");
				}
				line.deleteCharAt(line.length()-1);
				out.println(line.toString());
				out.flush();
			}
			out.println(divider.toString());
			out.flush();
			out.close();
		}
		catch (FileNotFoundException e)
		{
			Application.alert(Application.PROGRAM,e.getMessage());
		}
		
		return true;
	}

	protected void onOpen()
	{
		getContentPane().add(mkc = new DefaultMaskChooser(DefaultMaskChooser.SAVE_DIALOG,DefaultMaskChooser.FILES_ONLY,true));
		getContentPane().validate();
	}
}
