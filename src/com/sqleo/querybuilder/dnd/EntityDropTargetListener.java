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
 * EntityDropTargetListener.java
 *
 * Created on January 25, 2007, 5:09 PM
 *
 */

package com.sqleo.querybuilder.dnd;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DnDConstants;
import java.awt.datatransfer.*;

import com.sqleo.querybuilder.*;
import com.sqleo.querybuilder.beans.Entity;
import com.sqleo.querybuilder.syntax.QueryTokens;


/**
 * This class is used to manage drop tables into the design view from the objects view.
 * The accepted data flavor is an Entity instance.
 * @author gtoffoli
 */
public class EntityDropTargetListener implements DropTargetListener {


  private ViewDiagram viewDiagram = null;
  
  private static final DataFlavor entityDataFlavor = new DataFlavor( Entity.class, Entity.class.getName());
            
  public EntityDropTargetListener(ViewDiagram viewdiagram)
  {     
        super();
        this.setViewDiagram(viewdiagram);
  }

  public void dragEnter(DropTargetDragEvent event) {
    
    //int a = event.getDropAction();

    if (!isDragAcceptable(event)) {
      event.rejectDrag();
      return;
    }
  }

  public void dragExit(DropTargetEvent event) {
  }

  public void dragOver(DropTargetDragEvent event) { // you can provide visual
                            // feedback here
      
  }

  public void dropActionChanged(DropTargetDragEvent event) {
    if (!isDragAcceptable(event)) {
      event.rejectDrag();
      return;
    }
  }

  public void drop(DropTargetDropEvent event) {
    if (!isDropAcceptable(event)) {
      event.rejectDrop();
      return;
    }

    event.acceptDrop(DnDConstants.ACTION_COPY);

    Transferable transferable = event.getTransferable();
    
    try {
        Entity entity = (Entity)transferable.getTransferData(entityDataFlavor);
    
        QueryTokens.Table token = new QueryTokens.Table( entity.getSchema(), entity.getEntityName());
        DiagramLoader.run(DiagramLoader.DEFAULT, viewDiagram.getBuilder(),token,true);
    } catch (Exception ex)
    {
        ex.printStackTrace();
    }
    event.dropComplete(true);
  }

  public boolean isDropAcceptable(DropTargetDropEvent event) { // usually, you
   
            return event.getCurrentDataFlavorsAsList().contains( entityDataFlavor );
            /*
            Transferable transferable = event.getTransferable();
            DataFlavor[] dataFlavors = event.getCurrentDataFlavors();
            
            for (int i=0; i<dataFlavors.length; ++i)
            {
                DataFlavor dataFlavor = dataFlavors[i];
                if (dataFlavor.equals( entityDataFlavor ))
                {
                    return true;
                }
            }

            return false;
             */
  }
  
  public boolean isDragAcceptable(DropTargetDragEvent event) { // usually, you
   
            
           return event.getCurrentDataFlavorsAsList().contains( entityDataFlavor );
           /*
            Transferable transferable = event.getTransferable();
            DataFlavor[] dataFlavors = event.getCurrentDataFlavors();
            
            for (int i=0; i<dataFlavors.length; ++i)
            {
                DataFlavor dataFlavor = dataFlavors[i];
                if (dataFlavor.equals( entityDataFlavor ))
                {
                    return true;
                }
            }
            */
  }

    public ViewDiagram getViewDiagram() {
        return viewDiagram;
    }

    public void setViewDiagram(ViewDiagram viewDiagram) {
        this.viewDiagram = viewDiagram;
    }
    
}
