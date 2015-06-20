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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.sqleo.common.util.I18n;
import com.sqleo.environment.Preferences;

public abstract class AbstractDialogModal extends JDialog implements ActionListener, Runnable
{
	protected static final int INITIAL_WIDTH = 495;
	protected static final int INITIAL_HEIGHT = 355;
	
	protected Box bar;
	protected CommandButton btnClose;
	
	protected AbstractDialogModal(Dialog owner, String title)
	{
		super(owner);
		initComponent(owner, title, new Dimension(INITIAL_WIDTH,INITIAL_HEIGHT));
	}
	
	protected AbstractDialogModal(Component owner, String title)
	{
		this(owner, title, INITIAL_WIDTH, INITIAL_HEIGHT);
	}
	
	protected AbstractDialogModal(Component owner, String title, int width, int height)
	{
		this(owner, title, new Dimension(width,height));
	}
	
	protected AbstractDialogModal(Component owner, String title, Dimension size)
	{
		super(owner instanceof Frame ? (Frame)owner : (Frame)SwingUtilities.getAncestorOfClass(Frame.class, owner));
		initComponent(owner, title, size);
	}
	
	protected void initComponent(Component owner, String title, Dimension size)
	{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(size.width,size.height);
		setLocationRelativeTo(owner);
		setTitle(title);
		setModal(true);
		
		bar = new Box(BoxLayout.X_AXIS);
		bar.add(Box.createHorizontalGlue());
		bar.add(Box.createRigidArea(new Dimension(8,0)));
		btnClose = insertButton(2,I18n.getString("application.close","Close"));
		
		BorderLayoutPanel pnlContent = new BorderLayoutPanel(3,3);
		pnlContent.setBorder(new EmptyBorder(5,5,5,5));
		pnlContent.setComponentSouth(bar);
		setContentPane(pnlContent);
		
		WindowListener wl = new WindowAdapter()
		{
			public void windowOpened(WindowEvent we)
			{
				new Thread(AbstractDialogModal.this).start();
			}
		};
		this.addWindowListener(wl);
		this.getRootPane().registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
	@Override
	public void setSize(int w, int h) {
		super.setSize(Preferences.getScaledRowHeight(w),Preferences.getScaledRowHeight(h));
	}
	
	protected CommandButton insertButton(int idx,String text)
	{
		CommandButton cb = new CommandButton(text,this);
		bar.add(cb,idx);
		
		return cb;
	}
	
	protected void setBarEnabled(boolean b)
	{
		for(int i=0; i<bar.getComponentCount(); i++)
		{
			bar.getComponent(i).setEnabled(b);
		}
	}
	
	public final void run()
	{
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		setBarEnabled(false);
		onOpen();
		setBarEnabled(true);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	protected abstract void onOpen();
	
	public void actionPerformed(ActionEvent ae)
	{
		this.dispose();
	}
}