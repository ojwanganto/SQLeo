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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;

import com.sqleo.common.gui.TreeView;
import com.sqleo.common.util.Classpath;
import com.sqleo.environment.Application;


class MaskLibraries extends TreeView implements TreeWillExpandListener
{
	MaskLibraries()
	{
		addTreeWillExpandListener(this);
		loadLibraries();
	}
	
	void addLibrary(String filename,boolean reload)
	{
		DefaultMutableTreeNode root = this.getRootNode();
		root.add(new DefaultMutableTreeNode(filename,true));
		
		if(reload) reloadRoot();
	}
	
	void loadLibraries()
	{
		for(Iterator i=Classpath.getLibraries().iterator(); i.hasNext();)
			addLibrary(i.next().toString(),false);
			
		reloadRoot();
	}

	void setLibrary(String filename)
	{
		DefaultMutableTreeNode root = this.getRootNode();
		for(int i=0; i<root.getChildCount(); i++)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(i);
			if(node.toString().equals(filename))
			{
				this.setSelectionNode(node);
			}
		}
	}

	String getLibrary()
	{
		if(!this.isSelectionEmpty())
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.getSelectionPath().getPathComponent(1);
			return node.toString();
		}
		
		return null;
	}
	
	private String getClassName()
	{
		if(!this.isSelectionEmpty() && this.getSelectionPath().getPathCount() == 3)
		{
			String classname = this.getSelectionPath().getLastPathComponent().toString();
			return classname.substring(0,classname.indexOf(".class"));
		}
		
		return null;
	}
	
	boolean unload(UoDriver uoDv)
	{
		String classname = this.getClassName();
		if(classname!=null)	uoDv.classname	= classname;

		return true;		
	}
	
	private void onLibraryExpand(DefaultMutableTreeNode node)
	{
		if(node.getChildCount() > 0) return;
		
		try
		{
			ZipFile zf = new ZipFile(node.toString());
    
			for(Enumeration entries = zf.entries(); entries.hasMoreElements();)
			{
				String name = ((ZipEntry)entries.nextElement()).getName();
				if(name.endsWith(".class") && name.indexOf("$")==-1)
				{
					name = name.replace('/','.');
					node.add(new DefaultMutableTreeNode(name,false));
				}
			}
			
			this.reload(node);
		}
		catch(IOException e)
		{
			Application.println(e,true);
		}
	}
	
	public void treeWillCollapse(TreeExpansionEvent tee) throws ExpandVetoException {}

	public void treeWillExpand(TreeExpansionEvent tee)
		throws ExpandVetoException
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tee.getPath().getLastPathComponent();			
		onLibraryExpand(node);
	}
}