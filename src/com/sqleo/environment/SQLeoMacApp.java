/*
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2015 edinhojorge@users.sourceforge.net
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

import java.io.File;

import javax.imageio.ImageIO;

import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.QuitResponse;
import com.sqleo.environment.mdi.DialogAbout;
import com.sqleo.environment.mdi.DialogPreferences;

/**
 * @author edinhojorge@users.sourceforge.net
 * Main class to integrate with osx. It won't run in any other os
 * By using this class, those are the features:
 * - Add the application menu to mac main menu
 * - Add SQLeo to the app name
 * - Integrates mac menu application about to SQLeo about
 * - Integrates mac menu application preferentes to SQLeo preferences
 * - Integrates mac menu application quit (and CMD+Q) to SQLeo shutdown procedure
 * - Add SQLeo menu to dock
 * - Add SQLeo badge to CMD+TAB icon list
 */
public class SQLeoMacApp {

	// TODO: With this class, there are two main class to start. A reflection could be used to use one class only because of specific apple classes
	public static void main(String[] args) {
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SQLeo");
        System.setProperty("apple.awt.application.name", "SQLeo");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        
        // checking if any other LaF was sent in command line argument.
        // if none, try aqua as default for OSX
        if (System.getProperty("com.sqleo.laf.class") == null){
        	System.setProperty("com.sqleo.laf.class", _Constants.LAF_AQUA);
        }
        
        try {
			com.apple.eawt.Application macApplication = com.apple.eawt.Application.getApplication();
			
			macApplication.setAboutHandler(new com.apple.eawt.AboutHandler(){
				@Override
				public void handleAbout(AboutEvent e) {
					 new DialogAbout().setVisible(true);
				}
			});
			
			macApplication.setPreferencesHandler(new com.apple.eawt.PreferencesHandler(){
				@Override
				public void handlePreferences(PreferencesEvent e) {
					new DialogPreferences().setVisible(true);
				}
			});
			
			macApplication.setQuitHandler(new com.apple.eawt.QuitHandler(){
				@Override
				public void handleQuitRequestWith(QuitEvent e,
						QuitResponse arg1) {
					Application.shutdown();		
				}
			});

			
			macApplication.setDockIconImage(ImageIO.read(new File(SQLeoMacApp.class.getResource("/images/sqleo.png").toURI())));
			macApplication.setDockIconBadge("SQLeo");
		} catch (Exception e) {
			e.printStackTrace();
		}

        Application.main(args);

	}

}
