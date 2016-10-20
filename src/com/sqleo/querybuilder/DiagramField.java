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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.querybuilder.dnd.DragMouseAdapter;
import com.sqleo.querybuilder.dnd.RelationDropTargetListener;
import com.sqleo.querybuilder.dnd.RelationTransferHandler;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax._ReservedWords;


public class DiagramField extends JPanel implements ItemListener, MouseListener, PopupMenuListener
{
	private static long expr_counter = 0;
	
	int position;
	static ImageIcon keyIcon = null;
	private boolean primaryKey = false;
	private boolean inWhereClause = false;

	QueryTokens.Column querytoken;
	private DiagramAbstractEntity owner;

	private JCheckBox checkboxComponent = null;
	private JLabel labelComponent = null;

	private MouseListener listener;

	DiagramField(DiagramAbstractEntity entity, String label)
	{
		this(entity,label,false);
	}
	
	DiagramField(DiagramAbstractEntity entity, String label, boolean iskey)
	{
		super();
		setOwner(entity);
		setName(label);
		
		if (keyIcon == null)
			keyIcon = Application.resources.getIcon(Application.ICON_DIAG_FIELD);

		this.setLayout(new BorderLayout());
		checkboxComponent = new JCheckBox();

		labelComponent = new JLabel(label);
		labelComponent.setHorizontalTextPosition(JLabel.LEFT);
		
		Font f = labelComponent.getFont();
		if (!iskey)
		{
			f = new Font(f.getName(), Font.PLAIN, f.getSize());
		}
		else
		{
			primaryKey = true;
			f = new Font(f.getName(), Font.BOLD, f.getSize());
			labelComponent.setIcon(keyIcon);
		}
		labelComponent.setFont(f);

		this.add(getCheckboxComponent(), BorderLayout.WEST);
		this.add(labelComponent, BorderLayout.CENTER);

		getLabelComponent().addMouseListener(this);
		getLabelComponent().addMouseListener(listener = new DragMouseAdapter());
		getLabelComponent().setTransferHandler(new RelationTransferHandler());
		getLabelComponent().setDropTarget(new DropTarget(this, new RelationDropTargetListener(getOwner().builder.diagram)));

		getCheckboxComponent().addItemListener(this);
		getCheckboxComponent().addMouseListener(this);
		getCheckboxComponent().setPreferredSize(Preferences.getScaledDimension(20, 8));
		getCheckboxComponent().setBorderPainted(false);
		getCheckboxComponent().setFocusPainted(false);
		getCheckboxComponent().setOpaque(false);
		
		setOpaque(true);
		setBackground(ViewDiagram.BGCOLOR_DEFAULT);
		setBorder(new LineBorder(UIManager.getColor("List.background")));
	}
	
	public String getLabel()
	{
		return this.getName();
	}
	
	public void setToolTipText(String text)
	{
		getLabelComponent().setToolTipText(text);
	}
	
	public void setFontColor(final Color color){
		getLabelComponent().setForeground(color);
	}
	
	QueryTokens.Column getQueryToken()
	{
		return querytoken;
	}

	void setQueryToken(QueryTokens.Column token)
	{
		querytoken = token;
	}

	public void itemStateChanged(ItemEvent ie)
	{
		getOwner().onSelectionChanged(this);
	}

	public void mouseReleased(MouseEvent me)
	{
		if (SwingUtilities.isRightMouseButton(me))
		{
			JPopupMenu popup = new JPopupMenu(this.getName());
			popup.addPopupMenuListener(this);

			popup.add(new MenuItemSelect());
			popup.addSeparator();
			popup.add(new ActionAddWhere());
			popup.add(new ActionAddHaving());
			popup.addSeparator();
			popup.add(new ActionAddExpression());

			popup.show(this, me.getX(), me.getY());
		}
		else if (!this.getOwner().builder.isDragAndDropEnabled())
		{
			getOwner().builder.diagram.join(this.getOwner(),this,"=");
		}
	}

	public void mouseExited(MouseEvent me)
	{
	}
	public void mouseClicked(MouseEvent me)
	{
	}
	public void mouseEntered(MouseEvent me)
	{
	}
	public void mousePressed(MouseEvent me)
	{
	}

	public void popupMenuCanceled(PopupMenuEvent pme)
	{
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent pme)
	{
		setBorder(new LineBorder(UIManager.getColor("List.background")));
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent pme)
	{
		setBorder(new LineBorder(UIManager.getColor("List.selectionBackground")));
	}

	//	/////////////////////////////////////////////////////////////////////////////
	//	Join Manager
	//	/////////////////////////////////////////////////////////////////////////////
	private int joins;
	boolean isJoined()
	{
		return joins > 0;
	}

	void joined()
	{
		joins++;
	}

	void unjoined()
	{
		joins--;
	}

	void setDragAndDropEnabled(boolean b)
	{
		if(b)
			getLabelComponent().addMouseListener(listener);
		else
			getLabelComponent().removeMouseListener(listener);
	}

	//	/////////////////////////////////////////////////////////////////////////////
	//	Popup Actions
	//	/////////////////////////////////////////////////////////////////////////////
	private class MenuItemSelect extends JCheckBoxMenuItem implements ActionListener
	{
		private MenuItemSelect()
		{
			super("select");
			addActionListener(this);
			setState(DiagramField.this.isSelected());
		}

		public void actionPerformed(ActionEvent ae)
		{
			DiagramField.this.setSelected(this.getState());
		}
	}

	private class ActionAddExpression extends AbstractAction
	{
		private ActionAddExpression()
		{
			super(I18n.getString("querybuilder.menu.addExpression", "add expression..."));
		}

		public void actionPerformed(ActionEvent e)
		{
			final String sqleoGroupConcat = "SQLeoGroupConcat";
			final List<String> functions = new ArrayList<String>(Arrays.asList(SQLHelper.SQL_AGGREGATES));
			functions.add(sqleoGroupConcat);
			final JComboBox combo = new JComboBox(functions.toArray());
			final BorderLayoutPanel panel = new BorderLayoutPanel();
			panel.setComponentNorth(combo);
			JCheckBox pivotCheckbox = new JCheckBox(I18n.getString("querybuilder.message.sqleoPivotCheckbox", "Transform as Pivot"));
			panel.setComponentSouth(pivotCheckbox);
			panel.setPreferredSize(Preferences.getScaledDimension(200, 80));
			int value = JOptionPane.showOptionDialog(DiagramField.this.getOwner().builder,
					panel,
					I18n.getString("querybuilder.message.chooseFunction", "choose function:"),
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.PLAIN_MESSAGE,
                    null, null, null);
			if(JOptionPane.OK_OPTION != value)
				return;
			Object choose = combo.getSelectedItem();
			if (choose != null)
			{
				String expr = null;
				if(choose.toString().equals(sqleoGroupConcat)){
					Object sqleoGroupConcatSeparator =
						JOptionPane.showInputDialog(
							DiagramField.this.getOwner().builder,
							I18n.getString("querybuilder.message.sqleoGroupConcat.chooseSeparator", "choose separator:"),
							I18n.getString("querybuilder.menu.add", "add..."),
							JOptionPane.PLAIN_MESSAGE,
							null,
							null,
							null);
					if(null == sqleoGroupConcatSeparator){
						return;
					}
					if(!sqleoGroupConcatSeparator.toString().startsWith("'")){
						sqleoGroupConcatSeparator = "'"+sqleoGroupConcatSeparator+"'";
					}
					expr = sqleoGroupConcat + "(" + DiagramField.this.querytoken.getTable() +","+
							DiagramField.this.querytoken.getIdentifier() + "," + sqleoGroupConcatSeparator
							+ ")";
				}else{
					//pivot
					if(pivotCheckbox.isSelected()){
						Object sqleoPivotHeader =
							JOptionPane.showInputDialog(
								DiagramField.this.getOwner().builder,
								I18n.getString("querybuilder.message.sqleoPivotHeaderColumn", "Choose header column for Pivot:"),
								I18n.getString("querybuilder.menu.add", "add..."),
								JOptionPane.PLAIN_MESSAGE,
								null,
								null,
								null);
						if(null == sqleoPivotHeader){
							return;
						}
						expr = "SQLeoPivot(" + DiagramField.this.querytoken.getTable() +","+sqleoPivotHeader
							+ "," + choose.toString() + ","+ DiagramField.this.querytoken.getIdentifier() + ")";
						
					}else{
						expr = choose.toString() + "(" + DiagramField.this.querytoken.getIdentifier() + ")";
					}
				}
				QueryTokens.DefaultExpression token = new QueryTokens.DefaultExpression(expr);
				
				BrowserItems.AbstractQueryTreeItem qti = getOwner().builder.browser.getQueryItem();
				if(qti instanceof BrowserItems.DiagramQueryTreeItem)
				{
					token.setAlias("EXPR_" + (++expr_counter));
					
					DiagramQuery entityUp = ((BrowserItems.DiagramQueryTreeItem)qti).getDiagramObject();
					if(entityUp!=null){
						entityUp.addField(token.getAlias());
						entityUp.pack();
					}
				}
				
				getOwner().builder.browser.addSelectList(token);
			}
		}
	}

	private abstract class ActionAddCondition extends AbstractAction
	{
		abstract void add(QueryTokens.Condition token);
		abstract boolean isFirst();

		public void actionPerformed(ActionEvent e)
		{
			QueryTokens.Condition token = new QueryTokens.Condition();
			token.setLeft(new QueryTokens.DefaultExpression(DiagramField.this.querytoken.getIdentifier()));

			if (!isFirst())
				token.setAppend(_ReservedWords.AND);
			if (new MaskCondition(token, DiagramField.this.owner.builder).showDialog())
				add(token);
		}
	}

	private class ActionAddWhere extends ActionAddCondition
	{
		private ActionAddWhere()
		{
			putValue(NAME, I18n.getString("querybuilder.menu.addWhereCondition", "add where condition..."));
		}

		void add(QueryTokens.Condition token)
		{
			getOwner().builder.browser.addWhereClause(token);
			setWhereIcon();

		}

		boolean isFirst()
		{
			return getOwner().builder.browser.getQuerySpecification().getWhereClause().length == 0;
		}
	}
	
	public void setWhereIcon(){
		inWhereClause = true;
		labelComponent.setIcon(primaryKey? QueryModelTreeCellRenderer.keyAndWhereIcon : QueryModelTreeCellRenderer.whereIcon);
	}
	public void resetWhereIcon(){
		inWhereClause = false;
		labelComponent.setIcon(primaryKey ? keyIcon : null);
	}
	public boolean isInWhereClause(){
		return inWhereClause;
	}

	private class ActionAddHaving extends ActionAddCondition
	{
		private ActionAddHaving()
		{
			putValue(NAME, I18n.getString("querybuilder.menu.addHavingCondition", "add having condition..."));
		}

		void add(QueryTokens.Condition token)
		{
			getOwner().builder.browser.addHavingClause(token);
		}

		boolean isFirst()
		{
			return getOwner().builder.browser.getQuerySpecification().getHavingClause().length == 0;
		}
	}

	public JCheckBox getCheckboxComponent()
	{
		return checkboxComponent;
	}

	public void setCheckboxComponent(JCheckBox checkboxComponent)
	{
		this.checkboxComponent = checkboxComponent;
	}

	public JLabel getLabelComponent()
	{
		return labelComponent;
	}

	public void setLabelComponent(JLabel labelComponent)
	{
		this.labelComponent = labelComponent;
	}

	public boolean isSelected()
	{
		return getCheckboxComponent().isSelected();
	}

	public void setSelected(boolean b)
	{
		getCheckboxComponent().setSelected(b);
	}

	public DiagramAbstractEntity getOwner()
	{
		return owner;
	}

	public void setOwner(DiagramAbstractEntity owner)
	{
		this.owner = owner;
	}
}