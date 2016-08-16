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

package com.sqleo.environment.ctrl.content;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.CustomLineBorder;
import com.sqleo.environment.Application;


public abstract class AbstractMaskPerform extends BorderLayoutPanel
{
	static final short SQL = 0;
	static final short TXT = 1;
	static final short WEB = 2;
	static final short CSV = 3;
	static final short XLS = 4;

	protected JProgressBar progress;
	protected JButton btnStop;
	protected JLabel lblFile;
	protected JLabel lblMsg;
	
	protected ContentView view;
	
	public AbstractMaskPerform()
	{
		super(2,10);
		setBorder(new CompoundBorder(new CustomLineBorder(false,false,true,false), new EmptyBorder(20,10,15,10)));
		
		lblFile = new JLabel("File: <empty>");
		lblFile.setBorder(new CustomLineBorder(false,false,true,false));
		setComponentNorth(lblFile);
		
		btnStop = new JButton(Application.resources.getIcon(Application.ICON_STOP));
		btnStop.setEnabled(false);
		btnStop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				AbstractMaskPerform.this.lblMsg.setText("Stopped!");
				AbstractMaskPerform.this.btnStop.setEnabled(false);
				fireOnBtnStopClicked();
				
				AbstractMaskPerform.this.view.onTableChanged(true);
				AbstractMaskPerform.this.view.getControl().doRefreshStatus();
			}
		});
		
		progress = new JProgressBar();
		progress.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
				
		BorderLayoutPanel statusbar = new BorderLayoutPanel(2,2);
		statusbar.setComponentNorth(progress);
		statusbar.setComponentCenter(lblMsg = new JLabel("..."));
		statusbar.setComponentEast(btnStop);
		
		setComponentSouth(statusbar);
	}
	
	protected void fireOnBtnStopClicked() {
		
	}

	void setContent(ContentView view)
	{
		this.view = view;
	}

	abstract void setType(short type, String tname, String fname, boolean appendExtension);
	
//	-----------------------------------------------------------------------------------------
//	?????????????????????????????????????????????????????????????????????????????????????????
//	-----------------------------------------------------------------------------------------
	boolean aborted()
	{
		return !btnStop.isEnabled();
	}
	
	void init()
	{
		btnStop.setEnabled(true);
		lblMsg.setText("Wait...");
	}

	abstract void next();
	abstract boolean finished();
//	-----------------------------------------------------------------------------------------
//	-----------------------------------------------------------------------------------------
}
