/*
 * Copyright (C) 2005 - 2006 JasperSoft Corporation.  All rights reserved. 
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from JasperSoft,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 *
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  USA  02111-1307
 *
 *
 *
 *
 * QueryModelTreeCellRenderer.java
 * 
 * Created on 1 giugno 2003, 16.04
 *
 */

package com.sqleo.querybuilder;

import  javax.swing.tree.*;
import  javax.swing.*;

import com.sqleo.environment.Application;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax._ReservedWords;

import  java.awt.*;

/**
 *
 * @author  Administrator
 */
public class QueryModelTreeCellRenderer extends DefaultTreeCellRenderer
{
    static ImageIcon queryIcon;
    static ImageIcon whereIcon;
    static ImageIcon keyAndWhereIcon;
    static ImageIcon fromIcon;
    static ImageIcon selectIcon;
    
    static ImageIcon orderByIcon;
    static ImageIcon havingIcon;
    static ImageIcon fieldIcon;
    static ImageIcon expressionIcon;
    static ImageIcon groupByIcon;
    static ImageIcon tableIcon;
    
    static ImageIcon customFolderIcon;

    public QueryModelTreeCellRenderer()
    {
        super();
        
        if (queryIcon == null) queryIcon = Application.resources.getIcon(Application.ICON_QB_QUERY);
        if (whereIcon == null) whereIcon = Application.resources.getIcon(Application.ICON_QB_WHERE);
        if (keyAndWhereIcon == null) keyAndWhereIcon = Application.resources.getIcon(Application.ICON_QB_KEYANDWHERE);
        if (fromIcon == null) fromIcon = Application.resources.getIcon(Application.ICON_QB_FROM);
        if (selectIcon == null) selectIcon = Application.resources.getIcon(Application.ICON_QB_SELECT);
        if (tableIcon == null) tableIcon = Application.resources.getIcon(Application.ICON_QB_TABLE);

        if (orderByIcon == null) orderByIcon = Application.resources.getIcon(Application.ICON_QB_ORDER);
        if (groupByIcon == null) groupByIcon = Application.resources.getIcon(Application.ICON_QB_GROUP);
        if (havingIcon == null) havingIcon = Application.resources.getIcon(Application.ICON_QB_HAVING);
        if (fieldIcon == null) fieldIcon = Application.resources.getIcon(Application.ICON_QB_FIELD);
        if (expressionIcon == null) expressionIcon = Application.resources.getIcon(Application.ICON_QB_EXPR);
        
        if (customFolderIcon == null) customFolderIcon = Application.resources.getIcon(Application.ICON_QB_FOLDER);
        
        Font f = this.getFont();
        if (f!=null)
        {
            setFont(new Font(f.getName(), Font.PLAIN, f.getSize()) );
        }
    }

    public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus)
    {
        JLabel jlabel = (JLabel)super.getTreeCellRendererComponent(tree, value, sel,expanded, leaf, row,hasFocus);
        jlabel.setForeground( Color.BLACK);
        ImageIcon icon = getElementIcon(value);
        jlabel.setIcon(icon);

        jlabel.setToolTipText(null);

        return jlabel;
    }

    protected ImageIcon getElementIcon(Object value)
    {
        if (value instanceof BrowserItems.ClauseTreeItem
        && (value.toString().equals(_ReservedWords.SELECT)
        || value.toString().equals(_ReservedWords.SELECT + " " + _ReservedWords.DISTINCT))) return selectIcon;
                
        if (value instanceof BrowserItems.AbstractQueryTreeItem
        && !(value instanceof BrowserItems.ConditionQueryTreeItem)) return queryIcon;
        
        if (value instanceof BrowserItems.FromTreeItem) return fromIcon;
        
        if (value instanceof BrowserItems.ClauseTreeItem &&
            value.toString().equals(_ReservedWords.ORDER_BY)) return orderByIcon;
        
        if (value instanceof BrowserItems.ClauseTreeItem &&
            value.toString().equals(_ReservedWords.GROUP_BY)) return groupByIcon;
        
        if (value instanceof BrowserItems.ClauseTreeItem &&
            value.toString().equals(_ReservedWords.HAVING)) return havingIcon;
        
        if (value instanceof BrowserItems.ClauseTreeItem &&
            value.toString().equals(_ReservedWords.WHERE)) return whereIcon;
        
        if(value instanceof BrowserItems.DefaultTreeItem)
        {
        	if(((BrowserItems.DefaultTreeItem)value).isQueryToken())
        	{
        		if(((BrowserItems.DefaultTreeItem)value).getUserObject() instanceof QueryTokens.DefaultExpression) return expressionIcon;
        		if(((BrowserItems.DefaultTreeItem)value).getUserObject() instanceof QueryTokens.Column) return fieldIcon;
        		if(((BrowserItems.DefaultTreeItem)value).getUserObject() instanceof QueryTokens._TableReference) return tableIcon;
        	}
        }
        
        return customFolderIcon;
    }
}
