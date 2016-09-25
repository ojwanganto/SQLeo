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

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.environment.Preferences;
import com.sqleo.querybuilder.BrowserItems.AbstractQueryTreeItem;
import com.sqleo.querybuilder.syntax.DerivedTable;
import com.sqleo.querybuilder.syntax.QueryExpression;
import com.sqleo.querybuilder.syntax.QuerySpecification;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;
import com.sqleo.querybuilder.syntax.SubQuery;
import com.sqleo.querybuilder.syntax._ReservedWords;


public class ViewBrowser extends BorderLayoutPanel implements TreeSelectionListener
{
	private QueryBuilder builder;
	private JTree tree;
	
	ViewBrowser(QueryBuilder builder)
	{
		this.builder = builder;
		
		setComponentCenter(new JScrollPane(tree = new JTree(createTreeModel())));
		tree.addMouseListener(new BrowserPopup(builder));
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new QueryModelTreeCellRenderer());
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		
		final int oldRowHeight = tree.getRowHeight();
		final int newRowHeight = Preferences.getScaledRowHeight(oldRowHeight);
		if(newRowHeight != oldRowHeight){
			tree.setRowHeight(newRowHeight);
		}
		
		BrowserDnD.init(builder,tree);
		expandAll();
	}
	
	private void expandAll()
	{
		expandAll((TreeNode)tree.getModel().getRoot());
	}

	private void expandAll(TreeNode node)
	{
		expand(node);
		
		for(int i = 0; i < node.getChildCount(); i++)
			expandAll(node.getChildAt(i));
	}
	
	private void expand(TreeNode node)
	{
		TreePath path = new TreePath(((DefaultMutableTreeNode)node).getPath());
		tree.expandPath(path);
	}
	
	public void nodeChanged(TreeNode node)
	{
		((DefaultTreeModel)tree.getModel()).nodeChanged(node);
	}
	
	void reload(TreeNode node)
	{
		((DefaultTreeModel)tree.getModel()).reload(node);
	}

	void refreshSelection()
	{
		TreeNode node = getSelectedNode();
		nodeChanged(node);
	}
	
	TreeNode getSelectedNode(){
		return (TreeNode)tree.getSelectionPath().getLastPathComponent();
	}
	
	void setSelectedItem(BrowserItems.DefaultTreeItem item)
	{
		tree.setSelectionPath(new TreePath(item.getPath()));
	}

	private void add(int idx, QueryTokens.Join token)
	{
		BrowserItems.DefaultTreeItem item = (BrowserItems.DefaultTreeItem)this.getQueryItem().getChildAt(idx);

		for(int i=0; i<2; i++)
		{
			Object userObject = i==0 ? token.getPrimary().getTable() : token.getForeign().getTable();			
			BrowserItems.DefaultTreeItem child = item.findChild(userObject);
			
			if(child==null)
				child = item.findChild("{ " + userObject + " }");
			
			if(child==null)
				item.add(child = new BrowserItems.TableTreeItem(userObject));
			
			((BrowserItems.JoinHandler)child).joined();
		}
	}

	private void add(int idx, QueryTokens._Base token)
	{
		BrowserItems.DefaultTreeItem item = (BrowserItems.DefaultTreeItem)this.getQueryItem().getChildAt(idx);
		BrowserItems.AbstractQueryTreeItem qi = null;
		
		if(token instanceof QueryTokens.Join)
			add(idx,(QueryTokens.Join)token);
		else if(token instanceof QueryTokens.Table)
			item.add(new BrowserItems.TableTreeItem(token));
		else if(token instanceof DerivedTable)
			item.add(qi = new BrowserItems.DiagramQueryTreeItem((DerivedTable)token));
		else if(token instanceof SubQuery) {
			SubQuery sub = (SubQuery)token;
			String label = "SUBQUERY";
			if (sub.isAliasSet())
				item.add(qi = new BrowserItems.QueryTreeItem(label + SQLFormatter.SPACE + sub.getAlias(),(SubQuery)token));
			else
				item.add(qi = new BrowserItems.QueryTreeItem(label,(SubQuery)token));
		}
		else if(token instanceof QueryTokens.Condition && ((QueryTokens.Condition)token).getRight() instanceof SubQuery)
			item.add(qi = new BrowserItems.ConditionQueryTreeItem((QueryTokens.Condition)token));
		else
			item.addChild(token);
		
		if(builder.isLoading())
		{
			if(qi != null) onLoad(qi);
			return;
		}
		
		reload(item);
		expand(item);
	}
	
	private void add(int idx, QueryTokens._Base[] tokens)
	{
		for(int i=0; i<tokens.length; i++)
			add(idx,tokens[i]);
	}
	
	private void add(QueryTokens.Sort[] tokens)
	{
		BrowserItems.DefaultTreeItem root = (BrowserItems.DefaultTreeItem)tree.getModel().getRoot();
		BrowserItems.DefaultTreeItem item = (BrowserItems.DefaultTreeItem)root.getChildAt(1);
		
		for(int i=0; i<tokens.length; i++)
			item.addChild(tokens[i]);
	}
	
	private void remove(int idx, QueryTokens.Join token)
	{
		BrowserItems.DefaultTreeItem item = (BrowserItems.DefaultTreeItem)this.getQueryItem().getChildAt(idx);
		
		for(int i=0; i<2; i++)
		{
			QueryTokens.Table qtoken = i==0 ? token.getPrimary().getTable() : token.getForeign().getTable();
			BrowserItems.DefaultTreeItem child = item.findChild(qtoken);
			if(child!=null)
			{
				((BrowserItems.JoinHandler)child).unjoined();
				if(!((BrowserItems.JoinHandler)child).isJoined())
				{
					child.setUserObject(qtoken);
					if(child instanceof BrowserItems.TableTreeItem)
						getQuerySpecification().addFromClause(qtoken);
				}
			}
		}
	}

	private void remove(int idx, QueryTokens._Base token)
	{
		BrowserItems.DefaultTreeItem item = (BrowserItems.DefaultTreeItem)this.getQueryItem().getChildAt(idx);
		
		if(token instanceof QueryTokens._TableReference)
		{
			if(token instanceof QueryTokens.Join)
				remove(idx,(QueryTokens.Join)token);
			else if(token instanceof DerivedTable)
				item.removeChild(((DerivedTable)token).getAlias());
			else
				item.removeChild(token);
		}
		else if(token instanceof SubQuery || (token instanceof QueryTokens.Condition && ((QueryTokens.Condition)token).getRight() instanceof SubQuery))
		{
			BrowserItems.DefaultTreeItem itemSub = (BrowserItems.DefaultTreeItem)item.getParent();
			item = (BrowserItems.DefaultTreeItem)itemSub.getParent();
			
			tree.setSelectionPath(new TreePath(item.getPath()));
			itemSub.removeFromParent();
		}
		else
		{
			item.removeChild(token);
		}
		
		if(builder.isLoading()) return;
		
		reload(item);
		expand(item);
	}
	
	void addSelectList(QueryTokens._Expression token)
	{
		if(builder.isLoading()) return;
		
		this.getQuerySpecification().addSelectList(token);
		this.add(0,token);
	}

	void addFromClause(QueryTokens._TableReference token)
	{
		if(builder.isLoading()) return;
		
		if(token instanceof QueryTokens.Join)
		{
			this.getQuerySpecification().removeFromClause(((QueryTokens.Join)token).getPrimary().getTable());
			this.getQuerySpecification().removeFromClause(((QueryTokens.Join)token).getForeign().getTable());
		}
		
		this.getQuerySpecification().addFromClause(token);
		this.add(1,token);
	}

	void addWhereClause(QueryTokens.Condition token)
	{
		if(builder.isLoading()) return;
		
		if(token.getAppend() == null && this.getQuerySpecification().getWhereClause().length > 0)
			token.setAppend(_ReservedWords.AND);
			
		this.getQuerySpecification().addWhereClause(token);
		this.add(2,token);
	}

	void addGroupByClause(QueryTokens.Group token)
	{
		if(builder.isLoading()) return;
		
		this.getQuerySpecification().addGroupByClause(token);
		this.add(3,token);
	}

	void addHavingClause(QueryTokens.Condition token)
	{
		if(builder.isLoading()) return;
		
		if(token.getAppend() == null && this.getQuerySpecification().getHavingClause().length > 0)
			token.setAppend(_ReservedWords.AND);
			
		this.getQuerySpecification().addHavingClause(token);
		this.add(4,token);
	}

	void addOrderByClause(QueryTokens.Sort token)
	{
		if(builder.isLoading()) return;
		
		builder.getQueryModel().addOrderByClause(token);
		
		BrowserItems.DefaultTreeItem root = (BrowserItems.DefaultTreeItem)tree.getModel().getRoot();
		BrowserItems.DefaultTreeItem item = (BrowserItems.DefaultTreeItem)root.getChildAt(1);
		
		item.addChild(token);
			
		reload(item);
		expand(item);
	}
	
	void setUnion(QueryExpression qe)
	{
		BrowserItems.AbstractQueryTreeItem item = this.getQueryItem();
		
		if(qe==null)
		{
			BrowserItems.AbstractQueryTreeItem itemU = item;
			item = (BrowserItems.AbstractQueryTreeItem)item.getParent();
			 
			tree.setSelectionPath(new TreePath(item.getPath()));
			itemU.removeFromParent();
		}
		else
			item.add(new BrowserItems.UnionQueryTreeItem(qe));
		
		reload(item);
		expand(item);

		item.getQueryExpression().setUnion(qe);		
	}

	void removeSelectList(QueryTokens._Expression token)
	{
		QuerySpecification qs = this.getQuerySpecification();
		
		if(token instanceof SubQuery)
		{
			BrowserItems.AbstractQueryTreeItem itemSub = (BrowserItems.AbstractQueryTreeItem)tree.getSelectionPath().getLastPathComponent();
			BrowserItems.AbstractQueryTreeItem itemQry = (BrowserItems.AbstractQueryTreeItem)itemSub.getParent().getParent(); 
			qs = itemQry.getQueryExpression().getQuerySpecification();
		}
		
		qs.removeSelectList(token);
		this.remove(0,token);
	}

	void removeFromClause(QueryTokens._TableReference token)
	{
		this.getQuerySpecification().removeFromClause(token);
		this.remove(1,token);
	}

	void removeWhereClause(QueryTokens.Condition token)
	{
		QuerySpecification qs = this.getQuerySpecification();
		
		if(token.getRight() instanceof SubQuery && tree.getSelectionPath().getLastPathComponent() instanceof BrowserItems.AbstractQueryTreeItem)
		{
			BrowserItems.AbstractQueryTreeItem itemSub = (BrowserItems.AbstractQueryTreeItem)tree.getSelectionPath().getLastPathComponent();
			BrowserItems.AbstractQueryTreeItem itemQry = (BrowserItems.AbstractQueryTreeItem)itemSub.getParent().getParent(); 
			qs = itemQry.getQueryExpression().getQuerySpecification();
		}
		
		qs.removeWhereClause(token);
		if(qs.getWhereClause().length > 0)
			qs.getWhereClause()[0].setAppend(null);
		this.remove(2,token);
	}

	void removeGroupByClause(QueryTokens.Group token)
	{
		this.getQuerySpecification().removeGroupByClause(token);
		this.remove(3,token);
	}
	
	void removeHavingClause(QueryTokens.Condition token)
	{
		QuerySpecification qs = this.getQuerySpecification();
		
		if(token.getRight() instanceof SubQuery)
		{
			BrowserItems.AbstractQueryTreeItem itemSub = (BrowserItems.AbstractQueryTreeItem)tree.getSelectionPath().getLastPathComponent();
			BrowserItems.AbstractQueryTreeItem itemQry = (BrowserItems.AbstractQueryTreeItem)itemSub.getParent().getParent(); 
			qs = itemQry.getQueryExpression().getQuerySpecification();
		}
		
		qs.removeHavingClause(token);
		if(qs.getHavingClause().length > 0)
			qs.getHavingClause()[0].setAppend(null);
		this.remove(4,token);
	}

	void removeOrderByClause(QueryTokens.Sort token)
	{
		builder.getQueryModel().removeOrderByClause(token);
		
		BrowserItems.DefaultTreeItem root = (BrowserItems.DefaultTreeItem)tree.getModel().getRoot();
		BrowserItems.DefaultTreeItem item = (BrowserItems.DefaultTreeItem)root.getChildAt(1);
		
		item.removeChild(token);
			
		reload(item);
		expand(item);
	}
	
	QuerySpecification getQuerySpecification()
	{
		return this.getQueryExpression().getQuerySpecification();
	}
	
	QueryExpression getQueryExpression()
	{
		return this.getQueryItem().getQueryExpression();
	}

/*	----------------------------------------------------------------------------------------------------------------------
	al di sopra di questa linea non puntare direttamente la variabile, ma usare il metodo getQueryItem!
	---------------------------------------------------------------------------------------------------------------------- */
	private BrowserItems.AbstractQueryTreeItem queryItem;
	private DefaultTreeModel createTreeModel()
	{
		queryItem = null;
		
		BrowserItems.DefaultTreeItem root = new BrowserItems.DefaultTreeItem("QUERY-TREE-MODEL");
		root.add(new BrowserItems.QueryTreeItem("ROOTQUERY", builder.getQueryModel().getQueryExpression()));
		root.add(new BrowserItems.ClauseTreeItem(_ReservedWords.ORDER_BY));
		
		DefaultTreeModel model = new DefaultTreeModel(root)
		{
			public void nodesWereInserted(TreeNode node, int[] childIndices)
			{
				((BrowserItems.ClauseTreeItem)node).onDropPerformed(ViewBrowser.this.builder);
				this.reload(node);
			}
		};
		
		return model;
	}

	public BrowserItems.AbstractQueryTreeItem getRootQueryItem()
	{
		BrowserItems.DefaultTreeItem root = (BrowserItems.DefaultTreeItem)tree.getModel().getRoot();
		return (BrowserItems.AbstractQueryTreeItem)root.getFirstChild();
	}
	
	BrowserItems.AbstractQueryTreeItem getQueryItem()
	{
		return queryItem!=null ? queryItem : (queryItem = getRootQueryItem());
	}

	private void onLoad(BrowserItems.AbstractQueryTreeItem qi)
	{
		queryItem = qi;
		
		add(0,qi.getQueryExpression().getQuerySpecification().getSelectList());
		add(1,qi.getQueryExpression().getQuerySpecification().getFromClause());
		add(2,qi.getQueryExpression().getQuerySpecification().getWhereClause());
		add(3,qi.getQueryExpression().getQuerySpecification().getGroupByClause());
		add(4,qi.getQueryExpression().getQuerySpecification().getHavingClause());
		
		QueryExpression qe = qi.getQueryExpression().getUnion();
		if(qe!=null)
		{
			BrowserItems.AbstractQueryTreeItem itemU = new BrowserItems.UnionQueryTreeItem(qe);
			qi.add(itemU);
			onLoad(itemU);
		}
		
		if(qi.getParent()!=null && qi.getParent().getParent()!=null){
			queryItem = qi.getParent().getParent() instanceof AbstractQueryTreeItem ?  (AbstractQueryTreeItem)qi.getParent().getParent() : null;
		}else {
			queryItem = null;
		}
	}

	void onModelChanged()
	{
		tree.setModel(createTreeModel());
		onLoad(getRootQueryItem());
		expand(getRootQueryItem());
		add(builder.getQueryModel().getOrderByClause());
		tree.setSelectionPath(new TreePath(getRootQueryItem().getPath()));
	}
	
	//TreeSelectionListener
	public void valueChanged(TreeSelectionEvent e)
	{
		TreePath path = tree.getSelectionPath();
		if(path==null) return;
		
		BrowserItems.AbstractQueryTreeItem nextQueryItem = null;
		for(int i=0; i<path.getPathCount(); i++)
		{
			if(path.getPathComponent(i) instanceof BrowserItems.AbstractQueryTreeItem)
				nextQueryItem = (BrowserItems.AbstractQueryTreeItem)path.getPathComponent(i);
		}
		
		if(nextQueryItem==null)
			nextQueryItem = getRootQueryItem();
		
		if(queryItem!=null)
		{
			if(nextQueryItem == queryItem) return;
			
			if(nextQueryItem instanceof BrowserItems.DiagramQueryTreeItem)
			{
				DiagramQuery entity = builder.diagram.getEntity((DerivedTable)nextQueryItem.getQueryExpression());
				((BrowserItems.DiagramQueryTreeItem)nextQueryItem).setDiagramObject(entity);
			}
			
			/* unload desktop */
			BrowserItems.FromTreeItem item = (BrowserItems.FromTreeItem)queryItem.getChildAt(1);
			item.setSelected(false);
			nodeChanged(item);

			Component[] entities = builder.diagram.getEntities();
			Component[] relations = builder.diagram.getRelations();
			Component[] all = new JComponent[entities.length + relations.length];
			System.arraycopy(entities,0,all,0,entities.length);
			System.arraycopy(relations,0,all,entities.length,relations.length);
			
			// for freez diagram's entities location
			builder.getDiagramLayout();
			
			item.setDiagramObjects(all);
			for(int i=0; i<all.length; i++)
				builder.diagram.remove(all[i]);
			
			builder.diagram.repaint();
			builder.diagram.doResize();			
		}
		queryItem = nextQueryItem;
		
		/* load desktop */
		BrowserItems.FromTreeItem item = (BrowserItems.FromTreeItem)queryItem.getChildAt(1);
		item.setSelected(true);
		nodeChanged(item);
		
		Component[] toFlush = item.getDiagramObjects();
		if(toFlush!=null)
		{
			for(int i=0; i<toFlush.length; i++)
				builder.diagram.add(toFlush[i]);
		}
		else
		{
			builder.onLoad();
		}
		builder.diagram.doResize();
	}
}