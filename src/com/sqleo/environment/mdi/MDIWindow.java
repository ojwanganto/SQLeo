/*
 *
 * Modified by SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.UWindow;
import com.sqleo.common.util.MacKeyBindings;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment._Constants;


public class MDIWindow extends JFrame implements _Constants
{
    private JDesktopPane desktop;
    
    public MDIMenubar menubar;
    public MDIToolbar toolbar;
    
    private ArrayList connectionListeners;
    
    public JInternalFrame getDesktopTopFrame(){
       	final JInternalFrame[] frames = desktop.getAllFrames();
       	return frames.length>0 ? frames[0] : null;
    }
    
	public final void dispose()
	{
		ClientMetadataExplorer cme = (ClientMetadataExplorer)getClient(ClientMetadataExplorer.DEFAULT_TITLE);
		if(cme!=null) cme.dispose();
    	
		ClientCommandEditor cce = (ClientCommandEditor)getClient(ClientCommandEditor.DEFAULT_TITLE);
		if(cce!=null) cce.dispose();
    	
		Preferences.set("window.maximized", new Boolean(this.getExtendedState()==JFrame.MAXIMIZED_BOTH));
		if(this.getExtendedState()==JFrame.NORMAL)
		{
			Preferences.set("window.height"	,new Integer(this.getSize().height));
			Preferences.set("window.width"	,new Integer(this.getSize().width));
		}
		StringBuilder sb = new StringBuilder();
		int total = menubar.recentQueryMenu.getItemCount();
		for(int i = 0 ; i< total ;i++){
			sb.append(menubar.recentQueryMenu.getItem(i).getText());
			if(i<total-1){
				sb.append(MDIMenubar.RECENT_QUERY_SEPARATOR);
			}
		}
		Preferences.set(MDIMenubar.RECENT_QUERIES, sb.toString());
		
		super.dispose();
	}    
    
    public Action getAction(String key)
    {
        return this.getRootPane().getActionMap().get(key);
    }
    
    private void initActions()
    {
		this.getRootPane().getActionMap().put(ACTION_NEW_QUERY			, new MDIActions.NewQuery());
		this.getRootPane().getActionMap().put(ACTION_LOAD_QUERY			, new MDIActions.LoadQuery());
		this.getRootPane().getActionMap().put(ACTION_EXIT				, new MDIActions.Exit());
		this.getRootPane().getActionMap().put(ACTION_ABOUT				, new MDIActions.About());
		this.getRootPane().getActionMap().put(ACTION_HOWTOUSE			, new MDIActions.HowToUse());
		this.getRootPane().getActionMap().put(ACTION_SHORTCUTS			, new MDIActions.KeyboardShortcuts());
		
		this.getRootPane().getActionMap().put(ACTION_SHOW_PREFERENCES	, new MDIActions.ShowPreferences());
		this.getRootPane().getActionMap().put(ACTION_SHOW_CONTENT		, new MDIActions.ShowContent());
		this.getRootPane().getActionMap().put(ACTION_SHOW_DEFINITION	, new MDIActions.ShowDefinition());
		
        this.getRootPane().getActionMap().put(ACTION_MDI_SHOW_EXPLORER	, new MDIActions.ShowMetadataExplorer());
        this.getRootPane().getActionMap().put(ACTION_MDI_SHOW_HISTORY	, new MDIActions.ShowSQLHistoryViewer());
        this.getRootPane().getActionMap().put(ACTION_MDI_SHOW_EDITOR	, new MDIActions.ShowCommandEditor());
		this.getRootPane().getActionMap().put(ACTION_MDI_SHOW_COMPARER	, new MDIActions.ShowSchemaComparer());
		this.getRootPane().getActionMap().put(ACTION_MDI_SHOW_DATA_COMPARER	, new MDIActions.ShowDataComparer());

		this.getRootPane().getActionMap().put(ACTION_MDI_GOBACK	, new MDIActions.GoBack());
		this.getRootPane().getActionMap().put(ACTION_MDI_GOFWD	, new MDIActions.GoForward());		
		
		this.getRootPane().getActionMap().put(ACTION_MDI_CLOSE_ALL	, new MDIActions.CloseAllClients());
		this.getRootPane().getActionMap().put(ACTION_MDI_CASCADE	, new MDIActions.CascadeClients());
		this.getRootPane().getActionMap().put(ACTION_MDI_TILEH		, new MDIActions.TileClients());

		// This behavior is needed when:
		// - SO is a osx
		// - Look and feel is not aqua
		// SQLeo will behave like a mac app (CMD+C for copy and other actions)
		if (System.getProperty("osname", "none").indexOf("mac") > -1 
			&& javax.swing.UIManager.getLookAndFeel().equals(LAF_AQUA) == false){
			MacKeyBindings macKeyBindings = new MacKeyBindings();
			Object o[] = {
					"ComboBox.ancestorInputMap", macKeyBindings.getComboBoxInputMap(),
			        "EditorPane.focusInputMap", macKeyBindings.getMultiLineTextInputMap(),
			        "FormattedTextField.focusInputMap", macKeyBindings.getFormattedTextFieldInputMap(),
			        "List.focusInputMap", macKeyBindings.getListInputMap(),
			        "PasswordField.focusInputMap", macKeyBindings.getTextFieldInputMap(),
			        "ScrollBar.focusInputMap", macKeyBindings.getScrollBarInputMap(),
			        "ScrollBar.focusInputMap.RightToLeft", macKeyBindings.getScrollBarRightToLeftInputMap(),
			        "ScrollPane.ancestorInputMap", macKeyBindings.getScrollPaneInputMap(),
			        "Slider.focusInputMap", macKeyBindings.getSliderInputMap(),
			        "Slider.focusInputMap.RightToLeft", macKeyBindings.getSliderRightToLeftInputMap(),
			        "Spinner.ancestorInputMap", macKeyBindings.getSpinnerInputMap(),
			        "Table.ancestorInputMap", macKeyBindings.getTableInputMap(),
			        "Table.ancestorInputMap.RightToLeft", macKeyBindings.getTableRightToLeftInputMap(),
			        "TextArea.focusInputMap", macKeyBindings.getMultiLineTextInputMap(),
			        "TextField.focusInputMap", macKeyBindings.getTextFieldInputMap(),
			        "TextPane.focusInputMap", macKeyBindings.getMultiLineTextInputMap(),
			        "Tree.focusInputMap", macKeyBindings.getTreeInputMap(),
			        "Tree.focusInputMap.RightToLeft", macKeyBindings.getTreeRightToLeftInputMap()
				};
			UIManager.getDefaults().putDefaults(o);
		}
    
    
    }
    
    private void initComponents()
    {
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				Application.shutdown();
			}
		});
		
		setSize(Preferences.getInteger("window.width"),Preferences.getInteger("window.height"));
		UWindow.centerOnScreen(this);
		
		if(Preferences.getBoolean("window.maximized",false))
			setExtendedState(JFrame.MAXIMIZED_BOTH);

        BorderLayoutPanel content = new BorderLayoutPanel();
        content.setComponentNorth(toolbar = new MDIToolbar());
        content.setComponentCenter(desktop = new JDesktopPane());
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        
        setJMenuBar(menubar = new MDIMenubar());
        setContentPane(content);
        setTitle(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
    public final void show()
    {
		connectionListeners = new ArrayList();
    	
	        initActions();
		initComponents();
        
		super.show();
		
		add(new ClientMetadataExplorer());
    }
    
	public final void setTitle(String title)
	{
		if(title == null)
			title = Application.getVersion2();
		else
			title = Application.getVersion2() + " - " + title;
		
		super.setTitle(title);
	}
	
	public void add(MDIClient client)
	{
		desktop.add(client);
		try
		{
			client.addInternalFrameListener(menubar);
			client.setVisible(true);
			client.setMaximum(true);
			client.setSelected(true);
			client.setPreferences();
		}
		catch (PropertyVetoException e)
		{
			Application.println(e,false);
		}
	}

	public void closeAllClients()
	{
		JInternalFrame clients[] = desktop.getAllFrames();
	
		for (int i = 0; i < clients.length; i++)
		{
			if(clients[i].isClosable())
			{
				clients[i].doDefaultCloseAction();
			}
		}
	}
    
    public MDIClient getClient(String name)
    {
		JInternalFrame[] clients = desktop.getAllFrames();
		for(int i=0; i<clients.length; i++)
		{
			if(clients[i].getName()!=null && clients[i].getName().equals(name))	return (MDIClient)clients[i];
		}
		
		return null;
    }

    public boolean showClient(MDIClient client)
    {
    	if(client == null) return false;
    	
	    try
	    {
			client.setSelected(true);
	        client.toFront();
	    }
	    catch (PropertyVetoException e)
	    {
	        Application.println(e,false);
	    }
	    
		return true;
    }
    
    public boolean showClient(String name)
    {
		return showClient(this.getClient(name));
    }
    
    private MDIClient[] getClientsForResize()
    {
    	Vector clients = new Vector();
		JInternalFrame frames[] = desktop.getAllFrames();
		
		for(int i=0; i<frames.length; i++)
			if(frames[i].isMaximizable())
				clients.add(frames[i]);
		
		return (MDIClient[])clients.toArray(new MDIClient[clients.size()]);
    }
    
    
    public MDIClient[] getClientsOfConnection(String containsTitle,String keych)
    {
    	Vector clients = new Vector();
    	
		JInternalFrame[] clientFrames = desktop.getAllFrames();
		for(int i=0; i<clientFrames.length; i++)
		{
			if(clientFrames[i].getTitle().contains(containsTitle)){
				if(null == keych || clientFrames[i].getTitle().contains(keych)){
					clients.add((MDIClient)clientFrames[i]);
				}				
			}
		}
	
		return (MDIClient[])clients.toArray(new MDIClient[clients.size()]);
    }
    
	public void cascadeClients()
	{
		int offset = 20;
		int x = 0;
		int y = 0;
		
		MDIClient[] clients = getClientsForResize();
		
		int h = (desktop.getBounds().height - 5) - clients.length * offset;
		int w = (desktop.getBounds().width - 5) - clients.length * offset;
        
		for (int i = clients.length - 1; i >= 0; i--)
		{
			if(clients[i].isMaximizable() || clients[i].isResizable())
			{
				try
				{
					clients[i].setMaximum(false);
				}
				catch (PropertyVetoException e)
				{
					Application.println(e,false);
				}
				
				clients[i].setSize(w,h);
				clients[i].setLocation(x,y);
			
				x = x + offset;
				y = y + offset;
				
				showClient(clients[i]);
			}
		}
	}

	public void tileClients()
	{
		MDIClient[] clients = getClientsForResize();
        
		if(clients.length > 0)
		{
			int h = desktop.getBounds().height/clients.length;
			int w = desktop.getBounds().width;
			int y = 0;
        
			for (int i = 0; i < clients.length; i++)
			{
				if(clients[i].isMaximizable() || clients[i].isResizable())
				{
					try
					{
						clients[i].setMaximum(false);
					}
					catch (PropertyVetoException e)
					{
						Application.println(e,false);
					}
					
					clients[i].setSize(w,h);
					clients[i].setLocation(0,y);
					
					y = y + h;
				
					showClient(clients[i]);
				}
			}
		}
	}
    
	public void addListener(_ConnectionListener l)
	{
		connectionListeners.add(l);
	}
	
	public void removeListener(_ConnectionListener l)
	{
		connectionListeners.remove(l);
	}
    
	public void connectionClosed(String keycah)
	{
		for(Iterator iter = connectionListeners.iterator(); iter.hasNext();)
			((_ConnectionListener)iter.next()).onConnectionClosed(keycah);
	}
	
	public void connectionOpened(String keycah)
	{
		for(Iterator iter = connectionListeners.iterator(); iter.hasNext();)
			((_ConnectionListener)iter.next()).onConnectionOpened(keycah);
	}
}