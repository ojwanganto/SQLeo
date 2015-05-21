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

import java.awt.Point;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.sqleo.environment.Preferences;

public class TreeView extends BorderLayoutPanel
{
    private JTree tree;
    
    public TreeView()
    {
        this("<empty>",false);
    }
    
    public TreeView(String root, boolean visible)
    {
        setComponentCenter(new JScrollPane(tree = new JTree(new DefaultMutableTreeNode(root,true),true)));
        
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.setShowsRootHandles(true);
		tree.setRootVisible(visible);
		
		final int oldRowHeight = tree.getRowHeight();
		final int newRowHeight = Preferences.getScaledRowHeight(oldRowHeight);
		if(newRowHeight != oldRowHeight){
			tree.setRowHeight(newRowHeight);
		}
    }
    
	protected JTree getJavaComponent()
	{
		return tree;
	}    
    
    public void addTreeSelectionListener(TreeSelectionListener l)
    {
        tree.addTreeSelectionListener(l);
    }
    
    public void addTreeWillExpandListener(TreeWillExpandListener l)
    {
        tree.addTreeWillExpandListener(l);
    }
    
    public DefaultMutableTreeNode getRootNode()
    {
        return (DefaultMutableTreeNode)tree.getModel().getRoot();
    }

	public void clearSelection()
	{
		tree.clearSelection();
	}
	
	public boolean isSelectionEmpty()
	{
		return tree.isSelectionEmpty();
	}

	public TreePath getSelectionPath()
	{
		return tree.getSelectionPath();
	}
	
	public void setSelectionForLocation(Point p)
	{
		int row = tree.getRowForLocation((int)p.getX(),(int)p.getY());
		tree.setSelectionInterval(row,row);
	}

	public void setSelectionPath(TreePath path)
	{
		tree.setSelectionPath(path);
	}
    
	public DefaultMutableTreeNode getSelectionNode()
	{
		return (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
	}
	
	public void setSelectionNode(DefaultMutableTreeNode node)
	{
		tree.setSelectionPath(new TreePath(node.getPath()));
	}
    
	public void reloadRoot()
	{
		((DefaultTreeModel)tree.getModel()).reload();
	}
    
	public void reloadSelection()
	{
		if(tree.isSelectionEmpty()) return;
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.getSelectionPath().getLastPathComponent();
		reload(node);
	}

    public void reload(DefaultMutableTreeNode node)
    {
        ((DefaultTreeModel)tree.getModel()).reload(node);
    }
    
    public void removeNode(DefaultMutableTreeNode node)
    {
		((DefaultTreeModel)tree.getModel()).removeNodeFromParent(node);
    }
}