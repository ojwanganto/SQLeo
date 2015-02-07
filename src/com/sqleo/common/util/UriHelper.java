/*
 *
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2013 anudeepgade@users.sourceforge.net
 *  
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
package com.sqleo.common.util;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

public class UriHelper {
	
	
	public static void openUrl(final File file) {
		openURI(file.toURI());
	}
	public static void openUrl(final String url) {
		URI uri = null;
		try {
			uri = new URI(url);
			openURI(uri);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}
	public static void openURI(final URI uri) {
		if (Desktop.isDesktopSupported() && uri!=null) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(uri);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null,"Cannot connect to url",
						"Error opening link "+uri.getPath(), JOptionPane.WARNING_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"Java is not able to launch links on your computer.",
					"Cannot Launch Link "+uri.getPath(),JOptionPane.WARNING_MESSAGE);
		}
	}
}
