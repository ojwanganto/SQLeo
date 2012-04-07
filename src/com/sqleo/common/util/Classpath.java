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

package com.sqleo.common.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

public class Classpath
{
	private static Vector startup = new Vector();
	private static Vector runtime = new Vector();
	
	static
	{
		String bootPath	= System.getProperty("sun.boot.class.path");
		String classPath= System.getProperty("java.class.path");
		String extDir	= System.getProperty("java.ext.dirs");
		
		StringTokenizer tokenizer = new StringTokenizer(bootPath,";");
		while(tokenizer.hasMoreTokens())
		{
			String filename = tokenizer.nextToken();
			if(filename.endsWith(".zip") || filename.endsWith(".jar"))
				startup.addElement(filename);
		}
		
		tokenizer = new StringTokenizer(classPath,";");
		while(tokenizer.hasMoreTokens())
		{
			String filename = tokenizer.nextToken();
			if(filename.endsWith(".zip") || filename.endsWith(".jar"))
				startup.addElement(filename);
		}

		File fileExtDir = new File(extDir);
		String[] exts = fileExtDir.list();
		for(int i=0; exts!=null && i<exts.length; i++)
		{
			if(exts[i].endsWith(".zip") || exts[i].endsWith(".jar"))
				startup.addElement(new File(fileExtDir,exts[i]).toString());
		}
	}
	
	public static ArrayList getLibraries()
	{
		ArrayList libs = new ArrayList();
		
		libs.addAll(startup.subList(0,startup.size()));
		libs.addAll(runtime.subList(0,runtime.size()));
		
		return libs;
	}
	
	public static boolean isRuntime(String library)
	{
		if(Text.isEmpty(library) || startup.contains(library)) return false;
		
		if(!runtime.contains(library))
			runtime.addElement(library);
			
		return true;
	}
	
	public static ClassLoader loadLibrary(String library) throws MalformedURLException
	{
		if(Classpath.isRuntime(library))
			return new URLClassLoader(new URL[]{new File(library).toURL()},ClassLoader.getSystemClassLoader());
		
		return ClassLoader.getSystemClassLoader();
	}
}
