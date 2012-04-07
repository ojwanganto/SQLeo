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

package com.sqleo.querybuilder;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax._ReservedWords;


public class BrowserDnD implements DragGestureListener, DragSourceListener, DropTargetListener
{
	private QueryBuilder builder;
	private JTree tree;
	
	private TreePath pathSource;
	private TreePath pathLine;
	
	private Rectangle2D line  = new Rectangle2D.Float();
	
	private BrowserDnD(QueryBuilder builder,JTree tree)
	{
		this.builder = builder;
		this.tree = tree;
	}
	
	static void init(QueryBuilder builder,JTree tree)
	{
		BrowserDnD dnd = new BrowserDnD(builder,tree);
		
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_COPY_OR_MOVE, dnd);

		DropTarget dropTarget = new DropTarget(tree, dnd);
		dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);
	}
	
	/* DragSourceListener */
	public void dragEnter(DragSourceDragEvent dsde){}
	public void dragOver(DragSourceDragEvent dsde){}
	public void dropActionChanged(DragSourceDragEvent dsde){}
	public void dragDropEnd(DragSourceDropEvent dsde){tree.paintImmediately(line.getBounds());}
	public void dragExit(DragSourceEvent dse){}
	
	/* DropTargetListener */
	public void dropActionChanged(DropTargetDragEvent dtde){}
	public void dragEnter(DropTargetDragEvent dtde){}
	public void dragExit(DropTargetEvent dte){}
	
	public void dragOver(DropTargetDragEvent dtde)
	{
        // Avoid the drag of unknown objects....
        if (!dtde.getCurrentDataFlavorsAsList().contains( TransferableTreePath.TREEPATH_FLAVOR ))
        // 1.5 version if (!dtde.getDropTargetContext() Transferable().isDataFlavorSupported( TransferableTreePath.TREEPATH_FLAVOR )   )
        {
            dtde.rejectDrag();
            return;
        }
        
        //Point origin = dtde.getLocation();
		TreePath path = tree.getClosestPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
		
		if(pathLine!=null && pathLine.equals(path)) return;
		pathLine = path;
		
		tree.paintImmediately(line.getBounds());

		Rectangle area = tree.getPathBounds(pathLine);
		int y = area.y+(int)area.getHeight();
		
		line.setRect(0,y,tree.getWidth(),2);

		tree.getGraphics().setColor(Color.gray);
		tree.getGraphics().drawLine(0,y,tree.getWidth(),y);
	}

	public void drop(DropTargetDropEvent dtde)
	{
		boolean doChangePosition = false;
		
		TreePath pathAfter = tree.getClosestPathForLocation(dtde.getLocation().x, dtde.getLocation().y);

		int idxNew = 0;
		BrowserItems.DefaultTreeItem itemParent;
		BrowserItems.DefaultTreeItem itemSource = (BrowserItems.DefaultTreeItem)pathSource.getLastPathComponent();
		BrowserItems.DefaultTreeItem itemAfter = (BrowserItems.DefaultTreeItem)pathAfter.getLastPathComponent();
		
		if(itemAfter.isNodeSibling(itemSource))
		{
			itemParent = (BrowserItems.DefaultTreeItem)itemSource.getParent();
			
			int idxOld = itemParent.getIndex(itemSource);
			idxNew = itemParent.getIndex(itemAfter);
			
			doChangePosition = idxOld != idxNew;
			if(idxOld > idxNew) idxNew++;
		}
		else
		{
			itemParent = itemAfter;
			doChangePosition = itemParent.isNodeChild(itemSource);
		}

		if(doChangePosition)
		{
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			model.insertNodeInto(itemSource,itemParent,idxNew);
		}
		else if(itemSource.getParent().toString().indexOf(_ReservedWords.SELECT) != -1
			 && itemAfter.toString().indexOf(_ReservedWords.GROUP_BY) != -1
			 && itemSource instanceof BrowserItems.DefaultTreeItem)
		{
//			System.out.println("create 'group by' token!");
			
			QueryTokens._Expression token = (QueryTokens._Expression)itemSource.getUserObject();
			builder.browser.addGroupByClause(new QueryTokens.Group(token));
		}
		else if(itemSource.getParent().toString().indexOf(_ReservedWords.SELECT) != -1
			 && itemAfter.toString().indexOf(_ReservedWords.ORDER_BY) != -1
			 && itemSource instanceof BrowserItems.DefaultTreeItem)
		{
//			System.out.println("create 'order by' token!");
			
			QueryTokens._Expression token = (QueryTokens._Expression)itemSource.getUserObject();
			builder.browser.addOrderByClause(new QueryTokens.Sort(token));
		}
	}

	/* DragGestureListener */
	public void dragGestureRecognized(DragGestureEvent dge)
	{
		Point origin = dge.getDragOrigin();
		TreePath path = tree.getPathForLocation(origin.x,origin.y);
                
		if(path == null
		|| path.getParentPath().getLastPathComponent() instanceof BrowserItems.FromTreeItem
		|| path.getLastPathComponent() instanceof BrowserItems.FromTreeItem
		|| path.getLastPathComponent() instanceof BrowserItems.ClauseTreeItem
		|| path.getLastPathComponent() instanceof BrowserItems.AbstractQueryTreeItem)
			return;
		
		Rectangle area = tree.getPathBounds(path);

		boolean bExpanded = tree.isExpanded(path);
		boolean bLeaf = tree.getModel().isLeaf(path.getLastPathComponent());
		
		JLabel lbl = (JLabel)tree.getCellRenderer().getTreeCellRendererComponent(tree,path.getLastPathComponent(),false,bExpanded,bLeaf,0,false);
		lbl.setSize((int)area.getWidth(), (int)area.getHeight());

		BufferedImage imgGhost = new BufferedImage((int)area.getWidth(), (int)area.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = imgGhost.createGraphics();

		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
		lbl.paint(g2);

		Icon icon = lbl.getIcon();
		int nStartOfText = (icon == null) ? 0 : icon.getIconWidth()+lbl.getIconTextGap();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 0.5f));
		g2.setPaint(new GradientPaint(nStartOfText,	0, SystemColor.controlShadow, tree.getWidth(), 0, new Color(255,255,255,0)));
		g2.fillRect(nStartOfText, 0, tree.getWidth(), imgGhost.getHeight());
		g2.dispose();
        
		tree.setSelectionPath(path);
		
		Transferable transferable = new TransferableTreePath(pathSource = path);
		dge.startDrag(null, imgGhost, new Point(5,5), transferable, this);
	}
	
	private static class TransferableTreePath implements Transferable
	{
		public static final DataFlavor TREEPATH_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "TreePath");
		
		private DataFlavor[] flavors = {TREEPATH_FLAVOR};
		private TreePath path;

		public TransferableTreePath(TreePath path)
		{
			this.path = path;
		}

		public DataFlavor[] getTransferDataFlavors()
		{
			return flavors;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return java.util.Arrays.asList(flavors).contains(flavor);
		}

		public synchronized Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException
		{
			if (flavor.isMimeTypeEqual(TREEPATH_FLAVOR.getMimeType()))
				return path;
			else
				throw new UnsupportedFlavorException(flavor);	
		}
	}	
}