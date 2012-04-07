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
 * DragMouseAdapter.java
 *
 * Created on January 30, 2007, 4:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sqleo.querybuilder.dnd;

import java.awt.event.*;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @author gtoffoli
 */
public class DragMouseAdapter extends MouseAdapter {
    
    public void mousePressed(MouseEvent e) {
        
        if (e.getButton() == MouseEvent.BUTTON1)
        {
           JComponent c = (JComponent)e.getSource();
           TransferHandler handler = c.getTransferHandler();
           handler.exportAsDrag(c, e, TransferHandler.COPY);
        }
    }
}
