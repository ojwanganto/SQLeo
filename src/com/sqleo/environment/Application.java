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

import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.sqleo.common.util.Appearance;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.Resources;
import com.sqleo.common.util.Store;
import com.sqleo.common.util.Text;
import com.sqleo.environment.ctrl.commands.Command;
import com.sqleo.environment.ctrl.commands.CommandRunner;
import com.sqleo.environment.mdi.ClientMetadataExplorer;
import com.sqleo.environment.mdi.DialogPreferences;
import com.sqleo.environment.mdi.MDIWindow;
import com.sqleo.querybuilder.syntax._ReservedWords;


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
    private static String CONFIG_FOLDER = "";

    public static CommandRunner commandRunner;
    
    private static void loadIcons()
    {
	boolean Icon_scaling = false;

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
		resources.loadIcon(ICON_EXCEL	,"/images/export_excel.png");

		resources.loadIcon(ICON_COMPARER	,"/images/table_error.png");
        	resources.loadIcon(ICON_EXPLORER	,"/images/database_lightning.png");
        	resources.loadIcon(ICON_EDITOR		,"/images/page_edit.png");
		resources.loadIcon(ICON_PREFERENCES	,"/images/wrench.png");
        
		resources.loadIcon(ICON_EXPLORER_DRIVER_OK		,"/images/cog.png", Icon_scaling);
		resources.loadIcon(ICON_EXPLORER_DRIVER_KO		,"/images/cog_error.png", Icon_scaling);
		resources.loadIcon(ICON_EXPLORER_DRIVER_NEW		,"/images/cog_add.png");
		resources.loadIcon(ICON_EXPLORER_DATASOURCE_NEW	,"/images/database_add.png");
		resources.loadIcon(ICON_EXPLORER_DATASOURCE_OK	,"/images/database_connect.png", Icon_scaling);
		resources.loadIcon(ICON_EXPLORER_DATASOURCE_KO	,"/images/database.png", false);
		resources.loadIcon(ICON_EXPLORER_SCHEMA			,"/images/folder_database.png", Icon_scaling);
		resources.loadIcon(ICON_EXPLORER_TYPES			,"/images/folder_table.png", Icon_scaling);
		resources.loadIcon(ICON_EXPLORER_ALL			,"/images/table_multiple.png", Icon_scaling);
		resources.loadIcon(ICON_EXPLORER_LINKS			,"/images/table_link.png", Icon_scaling);
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
		resources.loadIcon(ICON_QUERY_DESIGNER,"/images/chart_organisation.png");

		resources.loadIcon(ICON_SQLEO,"/images/sqleo.png");
		resources.loadIcon(ICON_EXPORT_PIVOT, "/images/layout.png");
		
		//query builder tree icons 
		resources.loadIcon(ICON_QB_QUERY, "/images/page_white_database.png", Icon_scaling);
		resources.loadIcon(ICON_QB_WHERE, "/images/filter_where.png", Icon_scaling);
		resources.loadIcon(ICON_QB_KEYANDWHERE, "/images/bullet_key_filter.png", Icon_scaling);
		resources.loadIcon(ICON_QB_FROM, "/images/table_relationship.png", Icon_scaling);
		resources.loadIcon(ICON_QB_SELECT, "/images/layout.png", Icon_scaling);
		resources.loadIcon(ICON_QB_TABLE, "/images/database_table.png", Icon_scaling);
		resources.loadIcon(ICON_QB_ORDER, "/images/table_sort.png", Icon_scaling);
		resources.loadIcon(ICON_QB_GROUP, "/images/chart_organisation.png", Icon_scaling);
		resources.loadIcon(ICON_QB_HAVING, "/images/filter.png", Icon_scaling);
		resources.loadIcon(ICON_QB_FIELD, "/images/textfield.png", Icon_scaling);
		resources.loadIcon(ICON_QB_EXPR, "/images/sum.png", Icon_scaling);
		resources.loadIcon(ICON_QB_FOLDER, "/images/bullet_pink.png", Icon_scaling);
		
		//Diagram icons
		resources.loadIcon(ICON_DIAG_TABLE, "/images/database_table.png", Icon_scaling);
		resources.loadIcon(ICON_DIAG_FIELD, "/images/bullet_key.png", Icon_scaling);
		resources.loadIcon(ICON_DIAG_QUERY, "/images/page_white_database.png", Icon_scaling);
		resources.loadIcon(ICON_DIAG_OBJECT, "/images/database_table.png", Icon_scaling);

	}
	
	private static void loadSession()
	{
		try
		{
			if(new File(sessionXMLFilename()).exists() || new File(sessionMetaviewFilename()).exists())	{
				session.loadXMLAndMetaviews(sessionXMLFilename(),sessionMetaviewFilename());
			}else if(new File(sessionFilename()).exists()){
				session.load(sessionFilename());
			}
			if(new File(sessionSQLHistoryFilename()).exists()){
				session.loadSQLHistoryAsXml(sessionSQLHistoryFilename());
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
		final boolean askBeforeExit = Preferences.getBoolean(DialogPreferences.ASK_BEFORE_EXIT_KEY,true);
		if(!askBeforeExit || confirm(PROGRAM, message)){
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
				
				Application.initConfigFolder();
				session.saveXMLAndMetaviews(sessionXMLFilename(),sessionMetaviewFilename());
				session.saveSQLHistoryAsXml(sessionSQLHistoryFilename());
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
	
	//.sqleo file is the old file used , now replaced by sessionXMLFileName method .sqleo.xml
	@Deprecated
	private static String sessionFilename()
	{
		return System.getProperty("user.home") + File.separator + ".sqleo";
	}
	
	private static void initConfigFolder(){
		final String userDir = System.getProperty("user.home");
		final File configDir = new File(userDir, ".sqleo.config");
		if(!configDir.exists()){
			configDir.mkdir();
			// first time load config from old location - userDir
			CONFIG_FOLDER =  userDir;
		}else{
			CONFIG_FOLDER =  configDir.getAbsolutePath();
		}
	}
	
	private static String sessionXMLFilename()
	{
		return  CONFIG_FOLDER + File.separator + ".sqleo.xml";
	}
	
	private static String sessionMetaviewFilename()
	{
		return  CONFIG_FOLDER + File.separator + ".sqleo.metaview";
	}
	
	private static String sessionSQLHistoryFilename(){
		return  CONFIG_FOLDER + File.separator + ".sqleo.history.xml";
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
	
	public static void alertAsText(String message)
    {
       	final JTextArea jTextArea = new JTextArea(message);
       	jTextArea.setEditable(false);
		JOptionPane.showMessageDialog(window,jTextArea,PROGRAM,JOptionPane.WARNING_MESSAGE);
    }
	
	
	public static void alert(String message)
    {
       	JOptionPane.showMessageDialog(window,message,PROGRAM,JOptionPane.WARNING_MESSAGE);
    }
	
	public static void alertInfo(String message)
    {
       	JOptionPane.showMessageDialog(window,message,PROGRAM,JOptionPane.INFORMATION_MESSAGE);
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
    
    private static void initializeFontSize() {
        final int fontSizePercentage = Preferences.getInteger(DialogPreferences.FONT_SIZE_PERCENTAGE, DialogPreferences.DEFAULT_FONT_PERCENT);
        if (fontSizePercentage != 100) {
        	final float multiplier = fontSizePercentage / 100.0f;
        	final UIDefaults defaults = UIManager.getDefaults();
            for (Enumeration e = defaults.keys(); e.hasMoreElements();) {
            	final Object key = e.nextElement();
            	final Object value = defaults.get(key);
                if (value instanceof Font) {
                    Font font = (Font) value;
                    final int newSize = Math.round(font.getSize() * multiplier);
                    if (value instanceof FontUIResource) {
                        defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), newSize));
                    } else {
                        defaults.put(key, new Font(font.getName(), font.getStyle(), newSize));
                    }
                }
            }
            
            	final int fontSize = Math.round(13 * multiplier);
            	fontBOLD	= new Font("Dialog", Font.BOLD, fontSize);
            	fontPLAIN	= new Font("Dialog", Font.PLAIN, fontSize);
            	fontMONO	= new Font("monospaced", Font.PLAIN, fontSize);
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
        }else{
        	// By default, SQLeo uses  Metal LAF instead of OS native LAF  
        	try
            {
                javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            }
            catch (Exception e)
            {
                 e.printStackTrace();
            }
        	
        }
        System.out.println("SystemLookAndFeel: "+javax.swing.UIManager.getSystemLookAndFeelClassName());
        System.out.println("SQLeoUsingLookAndFeel: "+javax.swing.UIManager.getLookAndFeel());
        
        Application.initConfigFolder();
		Application.loadSession();
		Application.initI18n();
		Application.initializeFontSize();
		Application.initCommands();
		
		Application.println("Loading resources...");
		Application.loadIcons();
		Application.println("Loading window...");
        Application.window = new MDIWindow();
		//set icon
		JFrame.setDefaultLookAndFeelDecorated(true);
		// ticket #126: Compatibility Java Web Start
		//URL imgUrl = ClassLoader.getSystemResource("images/sqleo.png");
		// check if compatible with PNSPlugin 
        //URL imgUrl = Resources.class.getResource("/images/sqleo.png");
		Application.window.setIconImage(Application.resources.getIcon(Application.ICON_SQLEO).getImage());
		Application.window.show();
		
		JWindow wait = new JWindow(window);
		wait.getContentPane().add(new JLabel("Wait, auto connections...",JLabel.CENTER));
		wait.setSize(250,40);
		wait.setLocationRelativeTo(window);
		wait.setVisible(true);
		
		ClientMetadataExplorer cme = (ClientMetadataExplorer)window.getClient(ClientMetadataExplorer.DEFAULT_TITLE);
		cme.getControl().getNavigator().onFireAutoConnect();
		wait.dispose();
		
		if(Preferences.getBoolean(DialogPreferences.CHECK_FOR_UPDATE_KEY, true)){
			// add version link on menubar after window loading
			Application.window.menubar.addVersionLink();
		}
		
    }
    
    private static void initCommands() {
    	commandRunner = new CommandRunner();
    	for (final Command cmd : commandRunner.getCommands()) {
			_ReservedWords.ALL_RESERVED_WORDS.add(cmd.getCommand().toUpperCase());
		}
	}

	public static boolean isFullVersion() {
		return Application.MINOR.endsWith("+");
	}
}
