/*
 * Copyright (C) 2006 JasperSoft http://www.jaspersoft.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * ObjectsListCellRenderer.java
 *
 * Created on February 2, 2007, 5:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sqleo.querybuilder;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.sqleo.environment.Application;

/**
 *
 * @author gtoffoli
 */
public class ObjectsListCellRenderer extends DefaultListCellRenderer {
    
    static ImageIcon objectIcon = null;
    
    /** Creates a new instance of ObjectsListCellRenderer */
    public ObjectsListCellRenderer() {
        super();
        
        if (objectIcon == null) objectIcon = Application.resources.getIcon(Application.ICON_DIAG_OBJECT);

    }
    
    

    public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        java.awt.Component retValue;
        
        retValue = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        ((JLabel)retValue).setIcon(objectIcon );
        return retValue;
    }
    
}
