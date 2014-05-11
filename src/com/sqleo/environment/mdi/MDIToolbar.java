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

package com.sqleo.environment.mdi;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EtchedBorder;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.Toolbar;
import com.sqleo.environment.Application;


public class MDIToolbar extends BorderLayoutPanel
{
    public MDIToolbar()
    {
		Toolbar toolbar = new Toolbar(Toolbar.HORIZONTAL);		
		toolbar.add(Application.window.getAction(MDIActions.ACTION_MDI_GOBACK));
		toolbar.add(Application.window.getAction(MDIActions.ACTION_MDI_GOFWD));
		toolbar.addSeparator();
		toolbar.add(Application.window.getAction(MDIActions.ACTION_MDI_SHOW_EXPLORER));
		toolbar.add(Application.window.getAction(MDIActions.ACTION_MDI_SHOW_EDITOR));
		toolbar.add(Application.window.getAction(MDIActions.ACTION_NEW_QUERY));
		toolbar.addSeparator();
		toolbar.add(Application.window.getAction(MDIActions.ACTION_SHOW_PREFERENCES));
		toolbar.addSeparator();
		
		JPanel east = new JPanel();
		east.add(new MemoryBar());
		
		setComponentWest(toolbar);
		setComponentEast(east);
	}
    
	void onMDIClientActivated(MDIClient client)
	{
		if(getComponentCount() == 3)
			remove(2);
		
		if(client.getSubToolbar()!=null)
			setComponentCenter(client.getSubToolbar());

		repaint();
	}
	
	class MemoryBar extends JProgressBar implements Runnable
	{
		private Thread me;
        
		public MemoryBar()
		{
			setBorder(new EtchedBorder(EtchedBorder.LOWERED));
	    	
			addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					if(Application.confirm(Application.PROGRAM,"Run the garbage collector?")) System.gc();
				}
			});
            
			me = new Thread(this);
			me.start();
		}
        
		public Dimension getPreferredSize()
		{
			Dimension dim = super.getPreferredSize();
			dim.width = 100;
			return dim;
		}
        
		public Dimension getMaximumSize()
		{
			return this.getPreferredSize();
		}
        
		public void run()
		{
			while (true)
			{
				Runtime r = Runtime.getRuntime();
	    	    
				float freeMemory = r.freeMemory();
				float totalMemory = r.totalMemory();
	    	    
				int allocated = (int) (totalMemory / 1024.0F);
				int used = (int) ((totalMemory - freeMemory) / 1024.0F);
	    	    
				this.setMaximum(allocated);
				this.setValue(used);
	    	    
				String toolTip = NumberFormat.getInstance().format(used) + "K used";
				toolTip += " / " + (NumberFormat.getInstance().format(allocated) + "K allocated ");
				this.setToolTipText(toolTip);
				try
				{
					if (Thread.currentThread() == me)
						Thread.sleep(1975L);
				} 
				catch (InterruptedException e)
				{
					Application.println(e,false);
				}
			}
		}
	}
}