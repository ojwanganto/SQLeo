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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;


import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.editor.DialogFindReplace;
import com.sqleo.environment.mdi.ClientCommandEditor;

public abstract class QueryActions
{
	public static final String COPY_SYNTAX				= "copy-syntax";
	public static final String FIELDS_DRAGGABLE			= "fields-draggable";
	public static final String ENTITIES_ARRANGE_GRID		= "entities-arrange-grid";
	public static final String ENTITIES_ARRANGE_SPRING		= "entities-arrange-spring";
	public static final String ENTITIES_PACK			= "entities-pack";
	public static final String ENTITIES_REMOVE			= "entities-remove";
	public static final String DIAGRAM_SAVE_AS_IMAGE	= "diagram-save-as-image";
	public static final String FIND_AND_REPLACE	= "find-and-replace";
	
	static void init(QueryBuilder builder)
	{
		builder.getActionMap().put(COPY_SYNTAX			,new ActionCopySyntax(builder));
		builder.getActionMap().put(FIELDS_DRAGGABLE		,new ActionDragAndDrop(builder));
		builder.getActionMap().put(ENTITIES_ARRANGE_GRID	,new ActionArrangeEntitiesGrid(builder));
		builder.getActionMap().put(ENTITIES_ARRANGE_SPRING	,new ActionArrangeEntitiesSpring(builder));
		builder.getActionMap().put(ENTITIES_PACK		,new ActionPackEntities(builder));
		builder.getActionMap().put(ENTITIES_REMOVE		,new ActionRemoveEntities(builder));
		builder.getActionMap().put(DIAGRAM_SAVE_AS_IMAGE,new ActionSaveDiagramAsImage(builder));
		builder.getActionMap().put(FIND_AND_REPLACE,new ActionShowFindReplace(builder));

	}
	
	static class ActionShowFindReplace extends AbstractQueryAction {
		ActionShowFindReplace(QueryBuilder builder) {
			super(builder);
			putValue(SMALL_ICON,
					Application.resources.getIcon(Application.ICON_FIND));
			putValue(SHORT_DESCRIPTION, I18n.getString("datacontent.FindReplace","find/replace..."));
			putValue(NAME, "find/replace...");
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			DialogFindReplace dlg = builder.getClientQueryBuilder().getFindReplaceDialog();
			if (null == dlg) {
				dlg = new DialogFindReplace(builder.getSyntax());
				builder.getClientQueryBuilder().setFindReplaceDialog(dlg);
			}
			dlg.setVisible(true);
		}
	}
	
	abstract static class AbstractQueryAction extends AbstractAction
	{
		QueryBuilder builder;
		AbstractQueryAction(QueryBuilder builder){this.builder = builder;}
	}
	
	static class ActionDragAndDrop extends AbstractQueryAction
	{
		ActionDragAndDrop(QueryBuilder builder)
		{
			super(builder);
			putValue(NAME,I18n.getString("querybuilder.action.join","join by Drag&Drop"));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			builder.setDragAndDropEnabled(!builder.isDragAndDropEnabled());
		}
	}

	static class ActionCopySyntax extends AbstractQueryAction implements ClipboardOwner
	{
		ActionCopySyntax(QueryBuilder builder)
		{
			super(builder);
			putValue(NAME,I18n.getString("querybuilder.action.copySyntax","copy syntax"));
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			String value = builder.getSelectedIndex()==0 ? builder.getQueryModel().toString(true):
				builder.getSyntax().getText();
			
			Clipboard cb = builder.getToolkit().getSystemClipboard();
			StringSelection contents = new StringSelection(value);
			cb.setContents(contents,this);
		}

		public void lostOwnership(Clipboard clipboard, Transferable contents)
		{
		}
	}

	static class ActionArrangeEntitiesGrid extends AbstractQueryAction
	{
		ActionArrangeEntitiesGrid(QueryBuilder builder)
		{
			super(builder);
			putValue(NAME,I18n.getString("querybuilder.action.arrangeentitiesgrid","arrange grid"));
		}
        
		public void actionPerformed(ActionEvent e)
		{
			builder.diagram.doArrangeEntitiesGrid();
		}
	}
	
	static class ActionArrangeEntitiesSpring extends AbstractQueryAction
	{
		ActionArrangeEntitiesSpring(QueryBuilder builder)
		{
			super(builder);
			putValue(NAME,I18n.getString("querybuilder.action.arrangeentitiesspring","arrange spring"));
		}
        
		public void actionPerformed(ActionEvent e)
		{
			builder.diagram.doArrangeEntitiesSpring();
		}
	}

	static class ActionPackEntities extends AbstractQueryAction
	{
		ActionPackEntities(QueryBuilder builder)
		{
			super(builder);
			putValue(NAME,I18n.getString("querybuilder.action.packEntities","pack entities"));
		}
        
		public void actionPerformed(ActionEvent e)
		{
			DiagramAbstractEntity[] entities = builder.diagram.getEntities();
			for(int i=0; i<entities.length; i++)
				entities[i].setPack(true);
		}
	}
	
	static class ActionRemoveEntities extends AbstractQueryAction
	{
		ActionRemoveEntities(QueryBuilder builder)
		{
			super(builder);
			putValue(NAME,I18n.getString("querybuilder.action.removeEntities","remove entities"));
		}
        
		public void actionPerformed(ActionEvent e)
		{
			if(JOptionPane.showConfirmDialog(builder,I18n.getString("querybuilder.message.continue","do you want to continue?"), I18n.getString("querybuilder.action.removeEntities","remove entities"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) return;
			
			DiagramAbstractEntity[] entities = builder.diagram.getEntities();
			for(int i=0; i<entities.length; i++)
				entities[i].doDefaultCloseAction();
		}
	}

	static class ActionSaveDiagramAsImage extends AbstractQueryAction
	{
		ActionSaveDiagramAsImage(QueryBuilder builder)
		{
			super(builder);
			putValue(NAME,I18n.getString("querybuilder.action.saveDiagramAsImage","save as image"));
		}
	    
		public void actionPerformed(ActionEvent e)
		{
			String currentDirectory = Preferences.getString("lastDirectory",System.getProperty("user.home"));
			
			JFileChooser fc = new JFileChooser(currentDirectory);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
			fc.setFileFilter(new FileFilter()
			{
				public boolean accept(File file)
				{
					return file.isDirectory() || file.getName().endsWith(".jpeg");
				}
				public String getDescription()
				{
					return "JPEG (*.jpeg)";
				}
			});
		
			if(fc.showSaveDialog(builder) == JFileChooser.APPROVE_OPTION)
			{
				Preferences.set("lastDirectory",fc.getCurrentDirectory().toString());				
				String filename = fc.getSelectedFile().toString();
				
				if(!filename.endsWith(".jpeg")) filename += ".jpeg";				
				try
				{
					builder.diagram.saveAsImage(filename);
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}		
	}
}