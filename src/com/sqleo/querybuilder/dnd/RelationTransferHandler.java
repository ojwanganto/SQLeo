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
 * EntityTransferHandler.java
 *
 * Created on January 24, 2007, 4:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sqleo.querybuilder.dnd;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

import com.sqleo.querybuilder.DiagramAbstractEntity;
import com.sqleo.querybuilder.DiagramField;
import com.sqleo.querybuilder.beans.EntityField;


/**
 *
 * @author gtoffoli
 */
public class RelationTransferHandler extends TransferHandler
{    
    public int getSourceActions(JComponent c)
    {
             return COPY;
    }
        
    protected Transferable createTransferable(JComponent c)
    {
        if (c instanceof JLabel && c.getParent() instanceof DiagramField)
        {
            DiagramField df = (DiagramField)c.getParent();
            DiagramAbstractEntity de = (DiagramAbstractEntity)df.getOwner();
            
            // Look at the
            EntityField ef = new EntityField();
            ef.setTable(de.getQueryToken());
            ef.setFieldName(df.getName());
            
            return new TransferableObject(ef);           
        }
        
        return null;
    }
}
