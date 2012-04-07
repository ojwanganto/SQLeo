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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.gui.CommandButton;
import com.sqleo.environment.Application;


public class DialogDriver extends AbstractDialogConfirm
{
	public static final short ITEM_NEW = 0;
	public static final short ITEM_MODIFY = 1;
	public static final short ITEM_DUPLICATE = 2;
	
	private short request;
	private SideNavigator navigator;
	
	private UoDriver uoDv;
	
	private JPanel cardPanel;
	private CardLayout cardLayout;
	
	private JCheckBox cbxShowFileSystem;
	
	private int step = 0;
	
	private MaskLibraryChooser	mChoose;
	private MaskLibraries		mLibs;
	private MaskDriver			mDv;
	
	private CommandButton btnBack;
	
	public DialogDriver(SideNavigator navigator, short request)
	{
		super(Application.window,"driver." + (request == ITEM_MODIFY ? "edit":"new"));
		
		this.request = request;
		this.navigator = navigator;
		
		cardPanel = new JPanel();
		cardPanel.setLayout(cardLayout = new CardLayout());
		cardPanel.add("last", mDv = new MaskDriver());
		
		getContentPane().add(cardPanel);
		
		btnBack = insertButton(1,"< back");
		bar.add(cbxShowFileSystem = new JCheckBox("add library (browse filesystem)"),0);
	}
	
	protected void setBarEnabled(boolean b)
	{
		super.setBarEnabled(b);
		btnBack.setEnabled(step!=0);
	}
	
	private void showFirst()
	{
		step = 0;
		btnBack.setEnabled(false);
		btnConfirm.setText("next >");
		cardLayout.show(cardPanel,"first");
		cbxShowFileSystem.setVisible(true);
	}
	
	private void showNext()
	{
		step = 1;
		btnBack.setEnabled(true);
		cardLayout.show(cardPanel,"next");
		cbxShowFileSystem.setVisible(false);
	}
		
	private void showLast(boolean enabled)
	{
		step = 2;
		
		mDv.load(uoDv);
		mDv.setEnabled(enabled);
		
		btnBack.setEnabled(true);
		btnBack.setVisible(request!=ITEM_MODIFY);
		
		btnConfirm.setText("ok");
		cardLayout.show(cardPanel,"last");
		cbxShowFileSystem.setVisible(false);
	}
	
	protected void onOpen()
	{
		cardPanel.setVisible(false);
		
		if(request==ITEM_MODIFY)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)navigator.getSelectionPath().getLastPathComponent();
			mDv.load(uoDv = (UoDriver)node.getUserObject());
			
			boolean oneconnection=false;
			for(int i=0; !oneconnection && i<node.getChildCount(); i++)
			{
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
				oneconnection = ((UoDatasource)child.getUserObject()).isConnected();
			}
			
			showLast(!oneconnection);
		}
		else
		{
			uoDv = new UoDriver();
			
			cardPanel.add("first", mLibs	= new MaskLibraries());
			cardPanel.add("next" , mChoose	= new MaskLibraryChooser());
			
			if(request==ITEM_DUPLICATE)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)navigator.getSelectionPath().getLastPathComponent();
				mLibs.setLibrary(uoDv.library = ((UoDriver)node.getUserObject()).library);
				uoDv.classname = ((UoDriver)node.getUserObject()).classname;
				uoDv.example = ((UoDriver)node.getUserObject()).example;
				
				showLast(true);			
			}
			else
			{
				showFirst();			
			}
		}
		
		cardPanel.setVisible(true);
	}
	
	protected boolean onConfirm()
	{
		if(step == 0)
		{
			if(cbxShowFileSystem.isSelected())
				showNext();
			else if(findDriver(mLibs.getLibrary()) && mLibs.unload(uoDv))
				showLast(true);
		}
		else if(step == 1)
		{
			if(findDriver(mChoose.getLibrary()))
				showLast(true);
		}
		else if(mDv.unload(uoDv))
		{
			if(request==ITEM_MODIFY)
			{
				navigator.reloadSelection();
				return true;
			}
			else
			{
				navigator.add(uoDv);
				return true;
			}
		}
		return false;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnBack)
		{
			if(step == 2 && cbxShowFileSystem.isSelected())
				showNext();
			else 
				showFirst();
		}
		else
		{
			super.actionPerformed(ae);
		}
	}
	
	private boolean findDriver(String library)
	{
		if(library == null)
		{
			Application.alert(Application.PROGRAM,"select one library!");
			return false;
		}
		mDv.clearDrivers();
		
		try
		{
			ZipFile zf = new ZipFile(library);
			for(Enumeration entries = zf.entries(); entries.hasMoreElements();)
			{
				String name = ((ZipEntry)entries.nextElement()).getName();
				if(name.endsWith(".class") && name.indexOf("$")==-1 && name.toLowerCase().indexOf("driver")!=-1)
				{
					name = name.replace('/','.');
					mDv.addDriver(name.substring(0,name.indexOf(".class")));
				}
			}
			zf.close();
			
			uoDv.library = library;
			uoDv.classname = null;
		}
		catch(Exception ioe)
		{
			Application.println(ioe,true);
			return false;
		}
		
		return true;
	}	
}