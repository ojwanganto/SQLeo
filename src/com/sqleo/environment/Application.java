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

package com.sqleo.environment;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JWindow;

import com.sqleo.common.util.Appearance;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.Resources;
import com.sqleo.common.util.Store;
import com.sqleo.common.util.Text;
import com.sqleo.environment.mdi.ClientMetadataExplorer;
import com.sqleo.environment.mdi.MDIWindow;


public class Application extends Appearance implements _Constants,_Version
{
	public static final ClipboardOwner defaultClipboardOwner;
	
	static
	{
		defaultClipboardOwner = new ClipboardOwner()
		{	
			public void lostOwnership(Clipboard clipboard, Transferable contents)
			{
			}
		};	
	}
	
    public static MDIWindow window = null;
    
	public static final Store session		= new Store();
    public static final Resources resources	= new Resources();
    
    private static void loadIcons()
    {
		resources.loadIcon(ICON_CONNECT		,"/images/connect.png");
		resources.loadIcon(ICON_DISCONNECT	,"/images/disconnect.png");
		resources.loadIcon(ICON_ACCEPT	,"/images/accept.png");
    	resources.loadIcon(ICON_SAVE	,"/images/disk.png");
		resources.loadIcon(ICON_STOP	,"/images/stop.png");
		resources.loadIcon(ICON_FIND	,"/images/find.png");
		resources.loadIcon(ICON_FILTER	,"/images/filter.png");
		resources.loadIcon(ICON_DELETE	,"/images/cross.png");
		resources.loadIcon(ICON_BACK	,"/images/arrow_left.png");
		resources.loadIcon(ICON_FWD		,"/images/arrow_right.png");

		resources.loadIcon(ICON_COMPARER	,"/images/table_error.png");
        resources.loadIcon(ICON_EXPLORER	,"/images/database_lightning.png");
        resources.loadIcon(ICON_EDITOR		,"/images/page_edit.png");
		resources.loadIcon(ICON_PREFERENCES	,"/images/wrench.png");
        
		resources.loadIcon(ICON_EXPLORER_DRIVER_OK		,"/images/cog.png");
		resources.loadIcon(ICON_EXPLORER_DRIVER_KO		,"/images/cog_error.png");
		resources.loadIcon(ICON_EXPLORER_DRIVER_NEW		,"/images/cog_add.png");
		resources.loadIcon(ICON_EXPLORER_DATASOURCE_NEW	,"/images/database_add.png");
		resources.loadIcon(ICON_EXPLORER_DATASOURCE_OK	,"/images/database_connect.png");
		resources.loadIcon(ICON_EXPLORER_DATASOURCE_KO	,"/images/database.png");
		resources.loadIcon(ICON_EXPLORER_SCHEMA			,"/images/folder_database.png");
		resources.loadIcon(ICON_EXPLORER_TYPES			,"/images/folder_table.png");
		resources.loadIcon(ICON_EXPLORER_ALL			,"/images/table_multiple.png");
		resources.loadIcon(ICON_EXPLORER_LINKS			,"/images/table_link.png");
		resources.loadIcon(ICON_EXPLORER_ADD_GROUP		,"/images/cart_add.png");
		resources.loadIcon(ICON_EXPLORER_REMOVE_GROUP	,"/images/cart_delete.png");

		resources.loadIcon(ICON_EDITOR_OPEN ,"/images/folder_page.png");
		resources.loadIcon(ICON_EDITOR_SAVE ,"/images/page_save.png");
		resources.loadIcon(ICON_EDITOR_RUN	,"/images/page_gear.png");
		
		resources.loadIcon(ICON_CONTENT_UPDATE,"/images/database_save.png");
		resources.loadIcon(ICON_CONTENT_DELETE,"/images/table_row_delete.png");
		resources.loadIcon(ICON_CONTENT_INSERT,"/images/table_row_insert.png");
		
		resources.loadIcon(ICON_QUERY_LAUNCH,"/images/table_gear.png");
		resources.loadIcon(ICON_DIAGRAM_SAVE,"/images/picture_save.png");
	}
	
	private static void loadSession()
	{
		try
		{
			if(new File(sessionFilename()).exists())
			{
				session.load(sessionFilename());
			}
			Preferences.loadDefaults();
			
	    	if(Preferences.getBoolean("application.trace",false))
	    	{
		    	String temp = System.getProperty("java.io.tmpdir");
				System.setOut(new PrintStream(new FileOutputStream(new File(temp,"sqleo.out"))));
				System.setErr(new PrintStream(new FileOutputStream(new File(temp,"sqleo.err"))));
	    	}
	    	
			if(session.canMount(ENTRY_INFO))
			{
				session.mount(ENTRY_INFO);
				println("version: " + session.jump("version").get(0));
			}
		}
		catch (Exception e)
		{
			println(e,false);
		}
	}
	
	public static void shutdown()
	{
		final String message = I18n.getString("application.message.quit","Do you really want to quit SQLeo?");
		if(confirm(PROGRAM, message)){
			Application.window.dispose();
			try
			{
				session.mount(ENTRY_INFO);
				session.home();
				session.jump("version");
				
				if(session.jump().size() == 0)
					session.jump().add(getVersion());
				else
					session.jump().set(0,getVersion());
				
				session.save(sessionFilename());
			}
			catch (IOException e)
			{
				println(e,false);
			}
			finally
			{
				System.exit(0);
			}
		}
	}	
	
	private static String sessionFilename()
	{
		return System.getProperty("user.home") + File.separator + ".sqleo";
	}
	
    public static String getVersion()
    {
        return MAJOR + "." + MINOR;
    }
    
    public static String getVersion2()
    {
        return PROGRAM + "." + getVersion();
    }
    
    public static String getVersion3()
    {
        return getVersion2() + " [ " + WEB +" ]";
    }

	public static void alert(String title,String message)
    {
       	JOptionPane.showMessageDialog(window,message,title,JOptionPane.WARNING_MESSAGE);
    }
    
	public static boolean confirm(String title,String message)
	{
		return JOptionPane.showConfirmDialog(window,message,title,JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}    
    
	public static String input(String title,String message)
	{
		return JOptionPane.showInputDialog(window,message,title,JOptionPane.PLAIN_MESSAGE);
	}    
    
    public static void println(Exception e,boolean alert)
    {
    	if(alert)
    		alert(e.getClass().getName(), Text.wrap(e.toString(),100));
    	else
        	System.out.println(e);
    }
    
    public static void println(String s)
    {
        System.out.println(s);
    }
    
    public static void initI18n()
    {
       String s = Preferences.getString("app.locale","en_EN");
       
       if (s != null && s.length() > 0)
       {
            String language = s;
            if (s.indexOf("_") > 0) {
                language = s.substring(0, s.indexOf("_") );
                s = s.substring(s.indexOf("_")+1);
            }
            else s ="";
            
            String country = s;
            if (s.indexOf("_") > 0) {
                country = s.substring(0, s.indexOf("_") );
                s = s.substring(s.indexOf("_")+1);
            }
            else s ="";
            
            String variant = s;
           
            if (language.length()>0 && country.length() >0 && variant.length()>0)
            {
                I18n.setCurrentLocale(new Locale(language, country, variant));
            }
            else if (language.length()>0 && country.length()>0)
            {
                I18n.setCurrentLocale(new Locale(language, country));
            }
            else
            {
                I18n.setCurrentLocale(new Locale(language));
            }
       }
    }
    
    public static void main(String[] args)
    {    	
        // Fix for java Bug ID:  4521075
        System.setProperty("sun.swing.enableImprovedDragGesture","");        
        if(System.getProperty("com.sqleo.laf.class") != null)
        {
            try
            {
                javax.swing.UIManager.setLookAndFeel(System.getProperty("com.sqleo.laf.class"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("SystemLookAndFeel: "+javax.swing.UIManager.getSystemLookAndFeelClassName());
        
		Application.loadSession();
		Application.initI18n();
		Application.println("loading resources...");
		Application.loadIcons();
		Application.println("loading window...");
        Application.window = new MDIWindow();
		Application.window.show();
		
		JWindow wait = new JWindow(window);
		wait.getContentPane().add(new JLabel("wait, auto connections...",JLabel.CENTER));
		wait.setSize(250,40);
		wait.setLocationRelativeTo(window);
		wait.setVisible(true);
		
		ClientMetadataExplorer cme = (ClientMetadataExplorer)window.getClient(ClientMetadataExplorer.DEFAULT_TITLE);
		cme.getControl().getNavigator().onFireAutoConnect();
		wait.dispose();
    }
}