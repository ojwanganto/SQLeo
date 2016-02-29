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

package com.sqleo.environment.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.UriHelper;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment._Constants;
import com.sqleo.environment.ctrl.content.AbstractActionContent;
import com.sqleo.environment.ctrl.define.TableMetaData;
import com.sqleo.environment.io.FileHelper;
import com.sqleo.environment.io.FileStreamSQL;
import com.sqleo.querybuilder.DiagramLayout;
import com.sqleo.querybuilder.syntax.QueryExpression;
import com.sqleo.querybuilder.syntax.QueryTokens;


public abstract class MDIActions implements _Constants
{
    public static abstract class AbstractBase extends AbstractAction
    {
		public AbstractBase(){super();}
		public AbstractBase(String text){super(text);}
    	
		protected void setAccelerator(KeyStroke stroke)
		{
			putValue(ACCELERATOR_KEY,stroke);
		}

        protected void setIcon(String iconkey)
        {
        	putValue(SMALL_ICON,Application.resources.getIcon(iconkey));
        }
        
        protected void setText(String text)
        {
        	putValue(NAME,text);
        }
        
        protected void setTooltip(String text)
        {
        	putValue(SHORT_DESCRIPTION,text);
        }
    }

	public final static class Dummy extends AbstractAction
	{
		public Dummy(String text){super(text);}
		
		public void actionPerformed(ActionEvent ae)
		{
			Application.alert(Application.PROGRAM,"Not implemented!");
		}
	}

	public final static class HowToUse extends AbstractAction
	{
		public HowToUse(){super(I18n.getString("application.menu.help.howtouse","How to use..."));}
		public void actionPerformed(ActionEvent ae)
		{
			UriHelper.openUrl(Application.WEB);
		}
	}
	
	public final static class KeyboardShortcuts extends AbstractAction
	{
		public KeyboardShortcuts(){super(I18n.getString("application.menu.help.keyboardShortcuts","Keyboard shortcuts..."));}
		public void actionPerformed(ActionEvent ae)
		{
			new DialogKeyboardShortcuts().setVisible(true);
		}
	}
	
	public static class NewQuery extends AbstractBase
	{
		public NewQuery(){
			super();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_MASK));
			setIcon(ICON_QUERY_DESIGNER);
			setTooltip(I18n.getString("application.query_designer","Query designer"));
			setText(I18n.getString("application.menu.newQuery","New query"));
		}
		public void actionPerformed(ActionEvent ae)
		{
			if(!ConnectionAssistant.getHandlers().isEmpty())
			{
				Object keycah = null;
				if(ConnectionAssistant.getHandlers().size() > 1)
					keycah = JOptionPane.showInputDialog(Application.window,I18n.getString("application.message.useConnection","Use connection:"),Application.PROGRAM,JOptionPane.PLAIN_MESSAGE,null,ConnectionAssistant.getHandlers().toArray(),null);
				else
					keycah = ConnectionAssistant.getHandlers().toArray()[0];
				
				if(keycah != null)
				{
					DiagramLayout dl = new DiagramLayout();
					if(!Preferences.getBoolean("querybuilder.use-schema"))
					{
						ConnectionHandler ch = ConnectionAssistant.getHandler(keycah.toString());
						ArrayList schemas = (ArrayList)ch.getObject("$schema_names");
						if(schemas.size()>0)
						{
							Object schema = JOptionPane.showInputDialog(Application.window,I18n.getString("application.message.schema","Schema:"),Application.PROGRAM,JOptionPane.PLAIN_MESSAGE,null,schemas.toArray(),null);
							if(schema == null) return;
							dl.getQueryModel().setSchema(schema.toString());
						}
					}
				
					ClientQueryBuilder cqb = new ClientQueryBuilder(keycah.toString(),dl.getQueryModel().getSchema());
					cqb.setDiagramLayout(dl);
					
					Application.window.add(cqb);
				}
			}
			else
				Application.alert(Application.PROGRAM,I18n.getString("application.message.noConnection","No connection!"));
		}
	}
    
	public static class LoadQuery extends AbstractBase
	{
		public LoadQuery(){super(I18n.getString("application.menu.loadQuery","Load query..."));}
		
		private void setSchema(String schema, QueryExpression qe)
		{
			if(qe == null) return;
			
			QueryTokens._Base[] tokens = qe.getQuerySpecification().getSelectList();
			for(int i=0; i<tokens.length; i++)
			{
				if(tokens[i] instanceof QueryTokens.Column)
				{
					((QueryTokens.Column)tokens[i]).getTable().setSchema(schema);
				}
			}
			
			tokens = qe.getQuerySpecification().getFromClause();
			for(int i=0; i<tokens.length; i++)
			{
				if(tokens[i] instanceof QueryTokens.Join)
				{
					((QueryTokens.Join)tokens[i]).getPrimary().getTable().setSchema(schema);
					((QueryTokens.Join)tokens[i]).getForeign().getTable().setSchema(schema);
				}
				else
					((QueryTokens.Table)tokens[i]).setSchema(schema);
			}
			
			setSchema(schema,qe.getUnion());
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			Object[] ret = DialogQuery.showLoad();
			if(ret[0]!=null && ret[1]!=null && ret[2]!=null)
			{
				ClientQueryBuilder cqb = new ClientQueryBuilder(ret[2].toString());
				String fileName = ret[0].toString();
				cqb.setFileName(fileName);
				final boolean isSQLFile = cqb.isSQLFile();
				DiagramLayout dl = null;
				if(!isSQLFile){
				 dl = (DiagramLayout)ret[1];
				}
				
				/* gestire schema */
				if(Preferences.getBoolean("querybuilder.use-schema"))
				{
					if(dl!=null && dl.getQueryModel().getSchema()==null)
					{
						if(ret[3]!=null)
						{
							int option = JOptionPane.showConfirmDialog(Application.window,"Do you want to apply '" + ret[3] + "' schema on all elements?",Application.PROGRAM,JOptionPane.YES_NO_CANCEL_OPTION);
						
							if(option == JOptionPane.YES_OPTION)
								setSchema(ret[3].toString(),dl.getQueryModel().getQueryExpression());
							else if(option == JOptionPane.CANCEL_OPTION)
								return;
						}
					}
				}
				else
				{
					if(dl!=null){
						if(dl.getQueryModel().getSchema()==null)
						{
							if(ret[3]!=null)
							{
								dl.getQueryModel().setSchema(ret[3].toString());
								setSchema(null,dl.getQueryModel().getQueryExpression());
							}
						}
						else if(ret[3]!=null)
							dl.getQueryModel().setSchema(ret[3].toString());
					}else if(ret[3]!=null){
						cqb.setSchema(ret[3].toString());
					}
				}

				Application.window.add(cqb);
				
				if(!isSQLFile){
					cqb.setDiagramLayout(dl);
				}else {
					cqb.getQueryBuilder().setSelectedIndex(1);
					try {
						cqb.getQueryBuilder().getSyntax().setText(FileStreamSQL.readSQL(fileName));
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}
			}
		}
	}

	public static class LoadGivenQuery extends AbstractBase
	{
		public LoadGivenQuery(){super(I18n.getString("application.menu.loadQuery","Load query..."));}

		public void actionPerformed(ActionEvent ae)
		{
			String fileName = ae.getActionCommand();
			final File file = new File(fileName);
			if(!file.exists()){
				Application.alert(Application.getVersion2(),"File not found : "+fileName);
				return;
			}
			Application.window.menubar.addMenuItemAtFirst(fileName);
			final boolean isXmlFile = FileHelper.getFileExtension(fileName).equalsIgnoreCase("xml");
			final String[] options = new String[ isXmlFile ? 3 : 2 ];
			options[0] = "Query builder";
			options[1] = ClientCommandEditor.DEFAULT_TITLE;
			if(isXmlFile){
				options[2] = ClientDataComparer.DEFAULT_TITLE;
			}
			final Object selectedWindow
				= JOptionPane.showInputDialog(Application.window,I18n.getString("application.message.loadQueryInWindow","Load file in:")
					,Application.PROGRAM,JOptionPane.PLAIN_MESSAGE,null,options
					,null);
			
			if(ClientDataComparer.DEFAULT_TITLE.equals(selectedWindow.toString())){
				final Action action = Application.window.getAction(MDIActions.ACTION_MDI_SHOW_DATA_COMPARER);
				final ShowDataComparer showCmp = (ShowDataComparer) action;
				showCmp.actionPerformed(null);
				final ClientDataComparer comparer = (ClientDataComparer)
					Application.window.getClient(ClientDataComparer.DEFAULT_TITLE);
				comparer.loadSetupFile(file);

			}else if(!ConnectionAssistant.getHandlers().isEmpty()){
				Object keycah = null;
				if(ConnectionAssistant.getHandlers().size() > 1)
					keycah = JOptionPane.showInputDialog(Application.window,I18n.getString("application.message.useConnection","Use connection:"),Application.PROGRAM,JOptionPane.PLAIN_MESSAGE,null,ConnectionAssistant.getHandlers().toArray(),null);
				else
					keycah = ConnectionAssistant.getHandlers().toArray()[0];
				if(keycah != null){
					if("Query builder".equals(selectedWindow.toString())){
						loadFileInQueryBuilder(fileName, keycah);
					}else if(ClientCommandEditor.DEFAULT_TITLE.equals(selectedWindow.toString())){
						final Action action = Application.window.getAction(MDIActions.ACTION_MDI_SHOW_EDITOR);
						final ShowCommandEditor showCmd = (ShowCommandEditor) action;
						showCmd.actionPerformed(null);
						final ClientCommandEditor editor = (ClientCommandEditor)
							Application.window.getClient(ClientCommandEditor.DEFAULT_TITLE);
						editor.loadSQLFile(fileName, keycah.toString());
					}
				}
			}else{
				Application.alert(Application.window.getTitle(),"No connections exists!");
			}
		}

		private void loadFileInQueryBuilder(String fileName, Object keycah) {
			ClientQueryBuilder cqb = new ClientQueryBuilder(keycah.toString());
			cqb.setFileName(fileName);
			final boolean isSQLFile = cqb.isSQLFile();
			DiagramLayout dl = null;
			if(!isSQLFile){
				dl = DialogQuery.getDiagramLayoutForFile(fileName);
			}
			if(!Preferences.getBoolean("querybuilder.use-schema"))
			{
				ConnectionHandler ch = ConnectionAssistant.getHandler(keycah.toString());
				ArrayList schemas = (ArrayList)ch.getObject("$schema_names");
				if(schemas.size()>0)
				{
					Object schema = JOptionPane.showInputDialog(Application.window,I18n.getString("application.message.schema","Schema:"),Application.PROGRAM,JOptionPane.PLAIN_MESSAGE,null,schemas.toArray(),null);
					if(schema == null) return;
					if(!isSQLFile){
					 dl.getQueryModel().setSchema(schema.toString());
					}else{
						cqb.setSchema(schema.toString());
					}
				}
			}
			Application.window.add(cqb);
			if(!isSQLFile){
				cqb.setDiagramLayout(dl);
			}else {
				cqb.getQueryBuilder().setSelectedIndex(1);
				try {
					cqb.getQueryBuilder().getSyntax().setText(FileStreamSQL.readSQL(fileName));
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		}
	}

	public static class Exit extends AbstractBase
	{
		public Exit(){super(I18n.getString("application.menu.exit","Exit"));}
        
		public void actionPerformed(ActionEvent ae)
		{
			Application.shutdown();
		}
	}

	public static class GoBack extends AbstractBase
	{
		public GoBack()
		{
			super("Go back");
			setIcon(ICON_BACK);
			setTooltip(I18n.getString("application.back","Go back"));
			setEnabled(false);
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			Application.window.menubar.history.previous();
		}
	}

	public static class GoForward extends AbstractBase
	{
		public GoForward()
		{
			super("Go forward");
			setIcon(ICON_FWD);
			setTooltip(I18n.getString("application.next","Go forward"));
			setEnabled(false);
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			Application.window.menubar.history.next();
		}
	}

	public static class ShowContent extends AbstractActionContent
	{
		private TableMetaData tmd = null;
		
		public ShowContent(){this.putValue(NAME,I18n.getString("application.tool.content","Show content..."));}
        
		public void actionPerformed(ActionEvent e)
		{
			tmd = null;
			super.actionPerformed(e);
		}
		
		protected boolean isShowCountRecordsPopup(){
			return false;
		}
		
		protected void onActionPerformed(int records, int option)
		{
			if(option == JOptionPane.CANCEL_OPTION || (records == 0 && option == JOptionPane.NO_OPTION)) return;
			boolean retrieve = records > 0 && option == JOptionPane.YES_OPTION;
			
			ClientContent client = new ClientContent(this.getTableMetaData(),retrieve);
			client.setTitle(ClientContent.DEFAULT_TITLE+" : "+ this.getTableMetaData() + " : " + this.getTableMetaData().getHandlerKey());

			Application.window.add(client);
		}

		protected TableMetaData getTableMetaData()
		{
			if(tmd == null)
			{
				Object[] ret = DialogQuickObject.show("Show content");
				if(ret != null)
					tmd = new TableMetaData(ret[0].toString(), ret[1] == null ? null : ret[1].toString(), ret[2].toString());
			}
						
			return tmd;
		}
	}
	
	public static class ShowDefinition extends AbstractBase
	{
		public ShowDefinition(){super(I18n.getString("application.tool.definition","Show definition..."));}
        
		public void actionPerformed(ActionEvent ae)
		{
			Object[] ret = DialogQuickObject.show("Show definition");
			if(ret == null) return;
			
			String schema = ret[1] == null ? null : ret[1].toString();
			Application.window.add(new ClientDefinition(ret[0].toString(), new QueryTokens.Table(schema,ret[2].toString()), "TABLE"));
		}
	}	
		
	public static class ShowPreferences extends AbstractBase
	{
		public ShowPreferences()
		{
			super(I18n.getString("application.tool.preferences","Preferences"));
			setIcon(ICON_PREFERENCES);
			setTooltip(I18n.getString("application.tool.preferences","Preferences"));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			new DialogPreferences().setVisible(true);
		}
	}
    
    public static abstract class AbstractShow extends AbstractBase
    {
		public abstract String getMDIClientName();
        
        public void actionPerformed(ActionEvent ae)
        {
        	if(ae!=null) Application.window.menubar.history.enableSequence();
            Application.window.showClient(this.getMDIClientName());
        }
    }
    
    public static abstract class AbstractShowTool extends AbstractShow
    {
		public AbstractShowTool(KeyStroke ks, String iconKey)
		{
			setAccelerator(ks);
			setIcon(iconKey);
			setTooltip(this.getMDIClientName());
			setText(I18n.getString("application.tool.show","Show") + " " + this.getMDIClientName());
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			if(!Application.window.showClient(this.getMDIClientName()))
				Application.window.add(create());
		}
		
		protected abstract MDIClient create();
    }
    
    public static class ShowMetadataExplorer extends AbstractShowTool
    {
        public ShowMetadataExplorer()
        {
			super(KeyStroke.getKeyStroke(KeyEvent.VK_1,InputEvent.CTRL_MASK),ICON_EXPLORER);
        }
        
        public String getMDIClientName()
        {
			return ClientMetadataExplorer.DEFAULT_TITLE;
        }
        
		protected MDIClient create()
		{
			return new ClientMetadataExplorer();
		}        
    }
    
	public static class ShowCommandEditor extends AbstractShowTool
	{
		public ShowCommandEditor()
		{
			super(KeyStroke.getKeyStroke(KeyEvent.VK_2,InputEvent.CTRL_MASK),ICON_EDITOR);
		}
        
		public String getMDIClientName()
		{
			return ClientCommandEditor.DEFAULT_TITLE;
		}
		
		protected MDIClient create()
		{
			return new ClientCommandEditor();
		}        
	}

	public static class ShowSchemaComparer extends AbstractShowTool
	{
		public ShowSchemaComparer()
		{
			super(KeyStroke.getKeyStroke(KeyEvent.VK_3,InputEvent.CTRL_MASK),ICON_COMPARER);
		}
        
		public String getMDIClientName()
		{
			return ClientSchemaComparer.DEFAULT_TITLE;
		}
		
		protected MDIClient create()
		{
			return new ClientSchemaComparer();
		}        
	}
	
	 public static class ShowSQLHistoryViewer extends AbstractShowTool
	    {
	        public ShowSQLHistoryViewer()
	        {
				super(KeyStroke.getKeyStroke(KeyEvent.VK_4,InputEvent.CTRL_MASK),ICON_EXPLORER);
	        }
	        
	        public String getMDIClientName()
	        {
				return ClientSQLHistoryViewer.DEFAULT_TITLE;
	        }
	        
			protected MDIClient create()
			{
				return new ClientSQLHistoryViewer();
			}        
	    }
	 
	 public static class ShowDataComparer extends AbstractShowTool
	    {
	        public ShowDataComparer()
	        {
				super(KeyStroke.getKeyStroke(KeyEvent.VK_5,InputEvent.CTRL_MASK),ICON_EXPLORER);
	        }
	        
	        public String getMDIClientName()
	        {
				return ClientDataComparer.DEFAULT_TITLE;
	        }
	        
			protected MDIClient create()
			{
				return new ClientDataComparer();
			}        
	    }
	public static class CascadeClients extends AbstractBase
	{
		public CascadeClients()
		{
			super(I18n.getString("application.menu.cascade","Cascade"));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			Application.window.cascadeClients();  
		}
	}
    
	public static class TileClients extends AbstractBase
	{
		public TileClients()
		{
			super(I18n.getString("application.menu.tileHorizontal","Tile horizontal"));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			Application.window.tileClients();
		}
	}
    
	public static class CloseAllClients extends AbstractBase
	{
		public CloseAllClients()
		{
			super(I18n.getString("application.menu.closeAll","Close all"));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			Application.window.closeAllClients();
			Application.window.menubar.history.enableActions();
		}
	}
	
	public static class About extends AbstractBase
	{
		public About(){super(I18n.getFormattedString("application.menu.about","About {0}...", new Object[]{""+Application.PROGRAM}));}
        
		public void actionPerformed(ActionEvent ae)
		{
			  new DialogAbout().setVisible(true);
		}
	}
}